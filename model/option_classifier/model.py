import os
import pickle

import torch
import torch.nn as nn
from torch.utils.data import DataLoader
import matplotlib.pyplot as plt
from sklearn.metrics import precision_recall_curve
from option_classifier.settings import get_repo_path, overwrite_model


device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')


# Recurrent neural network (many-to-one)
class RNN(nn.Module):
    def __init__(self, input_size, embed_size, hidden_size, num_layers, num_classes):
        super(RNN, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers

        self.embed = nn.Embedding(input_size, embed_size)
        self.lstm = nn.LSTM(embed_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, num_classes)

    def forward(self, x, lens):
        embedded = self.embed(x)

        # Set initial hidden and cell states
        h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(device)
        c0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(device)

        # Forward propagate LSTM
        packed_seq = nn.utils.rnn.pack_padded_sequence(embedded, lens, batch_first=True, enforce_sorted=False)
        packed_out, _ = self.lstm(packed_seq, (h0, c0))  # out: tensor of shape (batch_size, seq_length, hidden_size)
        out, _ = nn.utils.rnn.pad_packed_sequence(packed_out, batch_first=True)

        gather_index = (lens - 1).unsqueeze(0).transpose(1,0).repeat((1,self.hidden_size)).unsqueeze(1).to(device)
        out_after_gather = torch.gather(out, 1, gather_index).squeeze()

        # Decode the hidden state of the last time step
        # probs = self.fc(out[:, -1, :])

        probs = self.fc(out_after_gather)
        return probs


def train(model, optimizer, criterion, train_loader, num_epochs):
    # Train the model
    total_step = len(train_loader)
    for epoch in range(num_epochs):
        for i, (codes, labels, lens) in enumerate(train_loader):
            codes = codes.to(device)
            labels = labels.to(device)

            # Forward pass
            outputs = model(codes, lens)
            loss = criterion(outputs, labels)

            # Backward and optimize
            optimizer.zero_grad()
            loss.backward()
            optimizer.step()

            if (i + 1) % 100 == 0:
                print('Epoch [{}/{}], Step [{}/{}], Loss: {:.4f}'
                      .format(epoch + 1, num_epochs, i + 1, total_step, loss.item()))


def evaluate(model, test_loader, repo_path):
    # Test the model
    model.eval()
    with torch.no_grad():
        y_labels = []
        y_probs = []
        correct = 0
        total = 0
        tp = 0
        tn = 0
        fp = 0
        fn = 0
        for codes, labels, lens in test_loader:
            codes = codes.to(device)
            labels = labels.to(device)
            outputs = model(codes, lens)
            _, predicted = torch.max(outputs.data, 1)

            y_probs.extend(outputs[:, 1].tolist())
            y_labels.extend(labels.tolist())

            _tp, _tn, _fp, _fn = compare_predictions_to_labels(predicted, labels)
            tp += _tp
            tn += _tn
            fp += _fp
            fn += _fn
            # total += labels.size(0)
            # correct += (predicted == labels).sum().item()

    print(tp, tn, fp, fn)
    precision = tp / (tp + fp)
    recall = tp / (tp + fn)
    f_score = 2 * precision * recall / (precision + recall)
    print('Test Precision of the model on the test dataset: {} %'.format(100 * precision))
    print('Test Recall of the model on the test dataset: {} %'.format(100 * recall))
    print('Test F Score of the model on the test dataset: {} %'.format(100 * f_score))

    p, r, t = precision_recall_curve(y_labels, y_probs, pos_label=1)
    plt.figure(figsize=(10, 10))
    plt.step(r, p, color='b', alpha=0.2, where='post')
    plt.fill_between(r, p, step='post', alpha=0.2,color='b')
    plt.xlabel('Recall')
    plt.ylabel('Precision')
    plt.ylim([0.0, 1.05])
    plt.xlim([0.0, 1.0])
    plt.xticks(list(map(lambda  x:x/20, range(0,21))))
    plt.yticks(list(map(lambda  x:x/20, range(0,21))))
    plt.title('2-class Precision-Recall curve')
    plt.grid()
    plt.savefig(repo_path + "/trained_models/pr_curve.jpg")

    # Save the model checkpoint
    if not os.path.isfile(repo_path + "/trained_models/model.ckpt"):
        torch.save(model.state_dict(), repo_path + "/trained_models/model.ckpt")


def compare_predictions_to_labels(predicted, labels):
    tp = 0
    tn = 0
    fp = 0
    fn = 0
    predicted = list(predicted)
    labels = list(labels)
    for i in range(len(labels)):
        if predicted[i] == labels[i]:
            if labels[i] == 1:
                tp += 1
            else:
                tn += 1
        else:
            if labels[i] == 1:
                fn += 1
            else:
                fp += 1
    return tp, tn, fp, fn


def run(repo_path):
    # Hyper-parameters
    embed_size = 128
    hidden_size = 128
    num_layers = 1
    num_classes = 2
    batch_size = 32
    num_epochs = 20
    learning_rate = 0.0003

    with open("./data/vocab.pkl", "rb") as file:
        word_vocab = pickle.load(file)
    with open(repo_path + "/dataset.pkl", "rb") as file:
        dataset = pickle.load(file)

    train_loader = DataLoader(dataset["train"], batch_size=batch_size, shuffle=True)
    test_loader = DataLoader(dataset["test"], batch_size=batch_size)
    weight = dataset["weight"]

    model = RNN(len(word_vocab), embed_size, hidden_size, num_layers, num_classes).to(device)

    # Loss and optimizer
    criterion = nn.CrossEntropyLoss(weight=torch.Tensor(weight).to(device))
    optimizer = torch.optim.Adam(model.parameters(), lr=learning_rate)

    if not overwrite_model() and os.path.isfile(repo_path + "/trained_models/model.ckpt"):
        model.load_state_dict(torch.load(repo_path + "/trained_models/model.ckpt"))
    else:
        train(model, optimizer, criterion, train_loader, num_epochs)
    evaluate(model, test_loader, repo_path)


if __name__ == "__main__":
    run(get_repo_path())
