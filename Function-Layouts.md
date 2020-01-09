EVM bytecode has a flat structure. It does not have explicit function entries, nor symbol tables. All executions starts from address `0x00`. 

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