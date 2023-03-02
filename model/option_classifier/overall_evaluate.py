from option_classifier.model import RNN, train, evaluate
import os
import pickle
import torch
import torch.nn as nn
from torch.utils.data import DataLoader, TensorDataset
from option_classifier.settings import overwrite_model

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')


def overall(repos):
    embed_size = 128
    hidden_size = 128
    num_layers = 1
    num_classes = 2
    batch_size = 32
    num_epochs = 20
    learning_rate = 0.0003

    with open("./data/vocab.pkl", "rb") as file:
        word_vocab = pickle.load(file)
    train_dataset = TensorDataset(torch.LongTensor([]), torch.LongTensor([]), torch.LongTensor([]))
    test_dataset = TensorDataset(torch.LongTensor([]), torch.LongTensor([]), torch.LongTensor([]))
    for repo in repos:
        with open("./data/" + repo + "/dataset.pkl", "rb") as file:
            dataset = pickle.load(file)
            train_dataset = train_dataset + dataset["train"]
            test_dataset = test_dataset + dataset["test"]

    train_loader = DataLoader(train_dataset, batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(test_dataset, batch_size=batch_size)
    weight = [0.1, 0.99]

    model = RNN(len(word_vocab), embed_size, hidden_size, num_layers, num_classes).to(device)

    # Loss and optimizer
    criterion = nn.CrossEntropyLoss(weight=torch.Tensor(weight).to(device))
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

    overall_repo_path = "./data/cross"
    if not overwrite_model() and os.path.isfile(overall_repo_path + "/trained_models/model.ckpt"):
        model.load_state_dict(overall_repo_path + "/trained_models/model.ckpt")
    else:
        train(model, optimizer, criterion, train_loader, num_epochs)
    evaluate(model, test_loader, overall_repo_path)


if __name__ == "__main__":
    overall(["hadoop","hbase"])