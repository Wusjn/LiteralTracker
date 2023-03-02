def get_repo():
    return "hbase"


def get_repo_path():
    return "./data/" + get_repo()


def get_repo_paths():
    return [
        "./data/hadoop",
        "./data/hbase"
    ]


def overwrite_model():
    return True
