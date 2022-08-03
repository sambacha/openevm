#### Address layout

EVM bytecode has a flat structure. It does not have explicit function entries, nor symbol tables. All executions starts from address `0x00`.

#### Limitations

Notice that at this moment this backend is limited to generate correct code for a single compilation unit.

In order to link more than one compilation units, one shall inline existing compilation units in the frontend so that the frontend can generate correct `main` (the `function dispatcher` function) for the whole smart contract.

#### The function dispatcher (meta function)

The `function dispatcher` function (usually called `main` function in some contexts) is always placed at the beginning of the generated binary bytecode. The dispatcher is responsible for:

1. parse the call data and find the called function address in the jump table using the hash value provided in the call data.
2. extract the call arguments, and push them on to stack.
3. call the function address specified in the jump table.

```
 Start of address
+---------------->  +-------------------------+
                    | Function dispatcher     |
                    |   Jump Table            |
                    |    (Func1,              |
                    |     Func2,              |
                    |     Func3)              |
                    +-------------------------+
                    |                         |
                    |      Func1              |
                    |                         |
                    +-------------------------+
                    |                         |
                    |      Func2              |
                    |                         |
                    +-------------------------+
                    |                         |
                    |      Func3              |
                    |                         |
                    +-------------------------+
```

#### Moving the function dispatcher to front of the LLVM IR function list

At this moment it is up to the frontend developer to move the LLVM IR function to the beginning of the function list. You can do something like this when creating function dispatcher:

```
// Let's say you have a dispatcher function named "dispatcher"

// You should include "llvm/IR/SymbolTableListTraits.h" here
using FunctionListType = SymbolTableList<Function>;
FunctionListType &FuncList = TheModule->getFunctionList();
FuncList.remove(dispatcher);
FuncList.insert(FuncList.begin(), dispatcher);
```
