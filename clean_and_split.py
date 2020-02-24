import glob
import os
import sys
import random
import javalang
import numpy as np
from tqdm import tqdm
from math import ceil 
from shutil import copyfile as cp

TRAIN_SPLIT = .8
TEST_VAL_SPLIT = .1

#minimum number of times the method name must be seen to include it in the dataset
MIN_NUM = 5

def copy_files(files, folder):
    for i in range(0, len(files)):
        cp(files[i], os.path.join(out_dir, folder, str(i) + ".java"))

def add_to_method_map(m_name):
    if m_name in m_names:
        m_names[m_name] += 1
    else:
        m_names[m_name] = 1

def get_all_methods(f_name):
    with open(f_name, "rb") as f:
        c = f.read()

    try:
        tree = javalang.parse.parse(c)
        methods = list(tree.filter(javalang.tree.MethodDeclaration))

    except (javalang.parser.JavaSyntaxError, AttributeError, javalang.tokenizer.LexerError, TypeError, RecursionError, StopIteration) as e:
        #print(e)
        return []

    return methods

def split_by_token(name):
    tokens = []
    token = ""
    prev = ""

    for c in name:
        if ((c.isupper() and prev.islower()) or c == "_" ) and len(token) > 0:
            tokens.append(token)
            token = c

        else:
            token += c

        prev = c


    if len(token) > 0:
        tokens.append(token)

    return tokens

if len(sys.argv) < 3:
    print("USAGE: python clean_and_split.py IN_DIR OUT_DIR")

data_dir = sys.argv[1]
out_dir = sys.argv[2]

split_or_clean = sys.argv[3]
split, clean, vec = False, False, False

if split_or_clean == "split":
    split = True
elif split_or_clean == "clean":
    clean = True
    vec_or_seq = sys.argv[4]
    if vec == "seq":
        vec = False
else:
    print("command not accepted")
    sys.exit(1)


all_files = []
m_names = {}

for (dirpath, dirnames, filenames) in os.walk(data_dir):
    all_files += [os.path.join(dirpath, _file) for  _file in filenames]

if clean:
    for _file in tqdm(all_files):
        methods = get_all_methods(_file)
        for path, node in methods:
            names = [node.name] if vec else split_by_token(node.name)

            for name in names:
                add_to_method_map(name)

    m_clean = {k: v for k, v in m_names.items() if v >= MIN_NUM}
    print("total", len(m_names), "clean", len(m_clean))

    s = ""
    for k, v in m_clean.items():
        s += k + "\n"

    with open("clean_names.txt", "w") as f:
        f.write(s)


#clean files here by putting each method in a new file?

if split:
    random.shuffle(all_files)

    l = len(all_files)
    end = ceil(TRAIN_SPLIT*l)
    train = all_files[0:end]

    start = end
    end = end + ceil(TEST_VAL_SPLIT*l)
    val = all_files[start:end]

    test = all_files[end:]


    if not os.path.exists(out_dir):
        os.mkdir(out_dir)
        os.mkdir(os.path.join(out_dir, "training"))
        os.mkdir(os.path.join(out_dir, "test"))
        os.mkdir(os.path.join(out_dir, "validation"))

    copy_files(train, "training")
    copy_files(test, "test")
    copy_files(val, "validation")
