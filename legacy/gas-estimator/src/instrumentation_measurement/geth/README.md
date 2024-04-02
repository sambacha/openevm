### `geth` instrumentation

See [here](/docs/notes/instrumentation_measurement/geth.md) for description and notes.

### Usage

0. Need to use `go-ethereum` with moved `CaptureState` in `github.com/ethereum/go-ethereum/core/vm/interpreter.go`, `CaptureState` must be after `execute`
1. `GOGC=off go run main.go --bytecode 62FFFFFF60002062FFFFFF600020`
