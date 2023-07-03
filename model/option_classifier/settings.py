def get_repo():
    return "hbase"


def get_repo_path():
    return "./data/" + get_repo()


def get_repo_paths():
    return [
        "./data/hadoop",
        "./data/hbase"
    ]

def get_dataset_dir():
    return "./data/datasets"

def overwrite_model():
    return False
