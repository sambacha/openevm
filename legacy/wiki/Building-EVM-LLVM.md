The project compiles like other LLVM projects. The target's name is `EVM`, but since it is not yet finalized, you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM` when you compile it.

In short, you can use the following to build the backend:

```
git clone git@github.com:etclabscore/evm_llvm.git
cd evm_llvm
git checkout EVM
mkdir build && cd build
cmake -DLLVM_TARGETS_TO_BUILD=EVM -DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM ..
make -j8
```
