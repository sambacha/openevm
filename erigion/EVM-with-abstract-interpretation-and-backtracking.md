# Goal

Describe what abstract interpretation and backtracking means, and how to implement it efficiently

# EVM resources

By resources here we understand things that programmer of EVM may use to store and manipulate data, and to perform computations. Some of the resources EVM are accessible directly via opcodes, whereas others - indirectly via side-effects of certain operations.

## Execution frames (substates)

When EVM is activated (which is usually) by sending a transaction to a deployed smart contract, with some input, the first execution frame is created. It gets its program counter, gas counter, "read only" flag (whether any mutating operations are allowed), input data in memory, and output data region in memory. Execution frame is mostly segregated from other execution frames, but there are few ways they can communicate:

1. Via input data
2. Via gas counter
3. Via storage writes (only for execution frames of the same contract)

## Stacks

Each execution frame has its own stack, and it is only accessible from that one execution frame.

## Memories

Each execution frame has its own memory. Memory expands in chunks of 32 bytes, when used, but is accessible with the granularity of a single byte.

## State caches

Whenever an item is read from the state, it potentially modifies state cache, which has an impact on the gas cost of subsequent operations with the same state item. Whenever a state item is created or updated, it also modifies state cache in a different way, which affects the cost of subsequent update operations for the same state item. State caches can be explicitly modelled as EVM resources for better specification and less error-prone implementation, but also for the purpose of implementing the backtracking.

## Access lists

Access lists are related to the state caches in a way that they pre-initialise read caches in a certain way.

## Self-destruct lists

Which accounts will be self-destructed and removed from the state at the end of a transaction. Self-destruct lists needs to be explicitly modelled for better specification and less error-prone implementation, but also for the purpose of implementing the backtracking.

## Block context

Timestamp, block hash, gas limit, base fee

## Transaction context

## Extra context

Recent block hashes

# Extended domain for stack elements

Abstract interpretation (as opposed to "concrete" interpretation) allows us to replace some concrete values on the stack (and then perhaps in memory or state caches) with `unknown` values. This effectively extends the domain of possible values from numbers `0`...`2^256-1` to also include `unknown` or potentially multiple types of unknown. For more rigorous approach, it may make sense to introduce at least two types of unknowns, one meaning that the value "does not exist", and another that the value is "unknown". The reason why we need "does not exist" is to perform the unification of stacks when resolving loops, for example. In order to describe the abstract interpretation a bit more formally, we need to define what a "stack" is (and then perhaps also what other resources are). The stack is the sequence of objects from the domain {`0`...`2^256-1`, `NE`, `NK`}, where `NE` means does not exist, and `NK` means not known. In order words, stack can be thought of a tuple of certain "maximum" size, for example, 100. So in our model, all possible stacks will be of fixed size (let's say 100), and to model a smaller stack, we fill the rest of elements with `NE` objects. For example, the stack

```
4
5
6
NK
7
```

will in fact be represented as

```
4
5
6
NK
7
NE
NE
...
NE
```

where the total length of the stack is 100. Why is it important that all stacks are of the same size? Because then we can define some operations on the domain of all possible stacks and express them in terms of operations on the individual elements.

Lets create an example of a simple loop:

```
0: PUSH1 10 # initial value of the loop counter
2: JUMPDEST # this is where iteration of the loop returns
3: PUSH1 1 # to perform counter--
5: SUB
6: DUP1 # make sure we don't destroy the only value of the counter by ISZERO
7: ISZERO # top of the stack is 1 if counter == 0
8: ISZERO # top of the stack is 1 is counter > 0
9: PUSH1 2
11: SWAP1
12: JUMPI # jump if still counter > 0
```

If we perform abstract interpretation of this program, this is how we could go. We start with an empty stack (full of `NE`s). As we go along, we create a mapping `PC (program counter) => stack`, which will help us understand whether we returned to the place we've been before.

```
NE
NE
...
NE
```

### 0: PUSH1 10

we shift the stack downwards and replace first element with 10 (the last `NE` gets discarded)

```
10
NE
...
NE
```

### 2: JUMPDEST

Nothing happens here, it is just a "goto label"

### 3: PUSH1 1

```
1
10
NE
...
NE
```

### 5: SUB

```
9
NE
...
NE
```

### 6: DUP1

```
9
9
NE
...
NE
```

### 7: ISZERO

```
0
9
NE
...
NE
```

### 8: ISZERO

```
1
9
NE
...
NE
```
