The project compiles like other LLVM projects. The target's name is `EVM`, but since it is not yet finalized, you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM` when you compile it.

In short,  you can use the following to build the backend:
```
git clone git@github.com:etclabscore/evm_llvm.git
cd evm_llvm
mkdir build && cd build
cmake -DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM ..
make -j8
```

In order to use the backend to generate EVM code, you have to specify `-mtriple=evm` when you call `llc`. An example is as follows:
```
./build/bin/llc -mtriple=evm -debug a.ll
```
