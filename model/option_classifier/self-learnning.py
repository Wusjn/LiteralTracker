from option_classifier.model import RNN, train, evaluate
import os
import pickle
import torch
import torch.nn as nn
from torch.utils.data import DataLoader
from option_classifier.settings import overwrite_model

device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')

def get_data_with_high_confidence(model, test_loader):


def self_learn(train_repo, test_repo):
    embed_size = 128
    hidden_size = 128
    num_layers = 1
    num_classes = 2
    batch_size = 32
    num_epochs = 10
    learning_rate = 0.0003

    with open("./data/vocab.pkl", "rb") as file:
        word_vocab = pickle.load(file)
    with open("./data/" + train_repo + "/dataset.pkl", "rb") as file:
        train_dataset = pickle.load(file)
    with open("./data/" + test_repo + "/dataset.pkl", "rb") as file:
        test_dataset = pickle.load(file)
    train_loader = DataLoader(train_dataset["train"] + train_dataset["test"], batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(test_dataset["train"] + test_dataset["test"], batch_size=batch_size)
    weight = train_dataset["weight"]

    model = RNN(len(word_vocab), embed_size, hidden_size, num_layers, num_classes).to(device)

    # Loss and optimizer
    criterion = nn.CrossEntropyLoss(weight=torch.Tensor(weight).to(device))
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

    cross_repo_path = "./data/cross/" + train_repo + "/" + test_repo
    if not overwrite_model() and os.path.isfile(cross_repo_path + "/trained_models/model.ckpt"):
        model.load_state_dict(cross_repo_path + "/trained_models/model.ckpt")
    else:
        train(model, optimizer, criterion, train_loader, num_epochs)
    evaluate(model, test_loader, cross_repo_path)


if __name__ == "__main__":
    cross("hadoop","hbase")
    cross("hbase","hadoop")