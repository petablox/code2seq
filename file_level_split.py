import glob
import os
import random
from math import ceil 
from shutil import copyfile as cp


data_dir = "/data2/edinella/java-small-og/"
out_dir = "/data2/edinella/java-small-og-fs/"

TRAIN_SPLIT = .8
TEST_VAL_SPLIT = .1

def copy_files(files, folder):
    for i in range(0, len(files)):
        cp(files[i], os.path.join(out_dir, folder, str(i) + ".java"))

all_files = []

for (dirpath, dirnames, filenames) in os.walk(data_dir):
    all_files += [os.path.join(dirpath, _file) for  _file in filenames]

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

