import pickle
from torch.utils.data import Dataset, DataLoader, TensorDataset
import torch
import numpy as np
import json
import random
from torchtext.vocab import vocab
from collections import Counter, OrderedDict
import pickle
from torchtext.data.utils import get_tokenizer
from option_classifier.settings import get_repo_paths

identifier_types = ["VariableName", "FieldName", "MethodName", "ParameterName", "PartOf", "Predicate"]


def highlight_label(code):
    new_code = []
    for token in code:
        if token in identifier_types:
            new_code.append("<sol>")
            new_code.append(token)
            new_code.append("<eol>")
        else:
            new_code.append(token)
    return new_code


def highlight_label_codes(codes):
    new_codes = []
    for code in codes:
        new_codes.append(highlight_label(code))
    return new_codes


def get_vocab():
    counter = Counter()
    counter.update(["<pad>"])
    counter.update(["<unk>"])
    counter.update(["<sol>"])
    counter.update(["<eol>"])
    for repo_path in get_repo_paths():
        with open(repo_path + "/raw_data/orpCodeList.json", "r", encoding="utf-8") as file:
            orp_tree_list = json.load(file)
        with open(repo_path + "/raw_data/literalCodeList.json", "r", encoding="utf-8") as file:
            literal_tree_list = json.load(file)
        for orp_tree in orp_tree_list:
            for orp_sample in orp_tree:
                counter.update(orp_sample)
        for literal_tree in literal_tree_list:
            for literal_sample in literal_tree:
                counter.update(literal_sample)

    sorted_by_freq_tuples = sorted(counter.items(), key=lambda x: x[1], reverse=True)
    ordered_word_dict = OrderedDict(sorted_by_freq_tuples)
    word_vocab = vocab(ordered_word_dict)
    return word_vocab


def run():
    word_vocab = get_vocab()
    with open("./data/vocab.pkl", "wb") as file:
        pickle.dump(word_vocab, file)

    pad_code = word_vocab["<pad>"]
    unk_code = word_vocab["<unk>"]
    sol_code = word_vocab["<sol>"]
    eol_code = word_vocab["<eol>"]
    sentence_length = 50

    for repo_path in get_repo_paths():
        print(repo_path)
        with open(repo_path + "/raw_data/orpCodeList.json", "r", encoding="utf-8") as file:
            orp_tree_list = json.load(file)
        with open(repo_path + "/raw_data/literalCodeList.json", "r", encoding="utf-8") as file:
            literal_tree_list = json.load(file)
        print(len(orp_tree_list), len(literal_tree_list))

        orp_percentage = len(orp_tree_list) / (len(orp_tree_list) + len(literal_tree_list))
        literal_percentage = 1 - orp_percentage
        print(orp_percentage, literal_percentage)
        # orp_list = highlight_label_codes(orp_list)
        # literal_list = highlight_label_codes(literal_list)

        code_tree_list = []
        label_list = []
        len_tree_list = []
        for orp_tree in orp_tree_list:
            code_tree = []
            len_tree = []
            for orp_sample in orp_tree:
                truncated_code = word_vocab(orp_sample[0:sentence_length])
                truncated_code = truncated_code + [pad_code] * (sentence_length - len(truncated_code))
                code_tree.append(truncated_code)
                len_tree.append(min(sentence_length, len(orp_sample)))
            code_tree_list.append(code_tree)
            label_list.append(1)
            len_tree_list.append(len_tree)
        for literal_tree in literal_tree_list:
            code_tree = []
            len_tree = []
            for literal_sample in literal_tree:
                truncated_code = word_vocab(literal_sample[0:sentence_length])
                truncated_code = truncated_code + [pad_code] * (sentence_length - len(truncated_code))
                code_tree.append(truncated_code)
                len_tree.append(min(sentence_length, len(literal_sample)))
            code_tree_list.append(code_tree)
            label_list.append(0)
            len_tree_list.append(len_tree)

        indices = range(len(code_tree_list))
        indices = np.random.permutation(indices)
        train_indices = indices[:int(len(indices) * 0.8)]
        test_indices = indices[int(len(indices) * 0.8):]

        train_x = []
        train_y = []
        train_len = []
        test_x = []
        test_y = []
        test_len = []

        for index in train_indices:
            for code, code_len in zip(code_tree_list[index], len_tree_list[index]):
                train_x.append(code)
                train_y.append(label_list[index])
                train_len.append(code_len)
        for index in test_indices:
            for code, code_len in zip(code_tree_list[index], len_tree_list[index]):
                test_x.append(code)
                test_y.append(label_list[index])
                test_len.append(code_len)

        train_dataset = TensorDataset(torch.LongTensor(train_x), torch.LongTensor(train_y), torch.LongTensor(train_len))
        test_dataset = TensorDataset(torch.LongTensor(test_x), torch.LongTensor(test_y), torch.LongTensor(test_len))
        dataset = {"train": train_dataset, "test": test_dataset, "weight": [orp_percentage, literal_percentage]}
        with open(repo_path + "/dataset.pkl", "wb") as file:
            pickle.dump(dataset, file)


if __name__ == "__main__":
    run()
