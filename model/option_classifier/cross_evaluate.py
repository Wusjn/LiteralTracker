from option_classifier.model import RNN, train, evaluate
import os
import pickle
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from option_classifier.settings import overwrite_model

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')


def cross(train_repos, test_repo):
    embed_size = 128
    hidden_size = 128
    num_layers = 1
    num_classes = 2
    batch_size = 32
    num_epochs = 10
    learning_rate = 0.0003

    with open("./data/vocab.pkl", "rb") as file:
        word_vocab = pickle.load(file)

    train_dataset = None
    for train_repo in train_repos:
        with open("./data/datasets/" + train_repo + ".pkl", "rb") as file:
            _dataset = pickle.load(file)
            if train_dataset is None:
                train_dataset = _dataset
            else:
                train_dataset["train"] += _dataset["train"]
                train_dataset["test"] += _dataset["test"]
                train_dataset["weight"][0] += _dataset["weight"][0]
                train_dataset["weight"][1] += _dataset["weight"][1]

    with open("./data/datasets/" + test_repo + ".pkl", "rb") as file:
        test_dataset = pickle.load(file)

    train_loader = DataLoader(train_dataset["train"] + train_dataset["test"], batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(test_dataset["train"] + test_dataset["test"], batch_size=batch_size)
    weight_sum = train_dataset["weight"][0] + train_dataset["weight"][1]
    weight = [train_dataset["weight"][0]/weight_sum, train_dataset["weight"][1]/weight_sum]

    model = RNN(len(word_vocab), embed_size, hidden_size, num_layers, num_classes).to(device)

    # Loss and optimizer
    criterion = nn.CrossEntropyLoss(weight=torch.Tensor(weight).to(device))
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

    cross_repo_path = "./data/cross"
    if not overwrite_model() and os.path.isfile(cross_repo_path + "/trained_models/" + test_repo + "_model.ckpt"):
        model.load_state_dict(torch.load(cross_repo_path + "/trained_models/" + test_repo + "_model.ckpt"))
    else:
        print(cross_repo_path + "/trained_models/" + test_repo + "_model.ckpt")
        train(model, optimizer, criterion, train_loader, num_epochs)
    evaluate(model, test_loader, test_repo, True)


if __name__ == "__main__":
    repos = set(["common", "hdfs", "yarn", "mapreduce", "hbase"])
    for test_repo in repos:
        train_repos = list(repos - set([test_repo]))
        cross(train_repos, test_repo)
