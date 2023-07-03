import pickle

with open("./data/config/vocab.pkl", "rb") as file:
    wordVocab = pickle.load(file)
with open("./data/config/dataset.pkl", "rb") as file:
    dataset = pickle.load(file)
pass
