# What is going on?
* LLVM backend for EVM target development happening inside the `EVM` branch.

# Compilation:
The project compiles like other LLVM projects. The target's name is `EVM`, and you have to specify `-DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM`. In short you can use the following to create build:
```
mkdir build
cmake -DLLVM_EXPERIMENTAL_TARGETS_TO_BUILD=EVM ..
make -j
```
