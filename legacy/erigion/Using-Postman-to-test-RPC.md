## Introduction

In this article, we provide instructions for using [Postman](https://www.postman.com/) to test Erigon's RPC interfaces. We begin with instructions for how to get started, we then explain a few thoughts on using Postman, and finally, we list a few other things we can do with the collection file in the future.

## Getting Started

### Install Postman and Erigon

In these instructions, we assume you have Postman installed and are logged in. You may have to create a user account on their website in order to download the software. You do not need to create or join a team.

We also assume that you have a copy of the Erigon source code in a folder called `$ERIGON`.

### Import the RPC Testing Collection

Once you have Postman running:

-   Click on the `Import` button at the top left of the screen. This will open the Import Dialog.
-   Click on the `File` tab and then the `Upload Files` button in the middle of the screen.
-   Navigate to the folder `$ERIGON/cmd/rpcdaemon/postman`.
-   Select the file `RPC_Testing.json` and confirm the import.
-   Click on the `Collections` subtab just below the main menu.

You should now have an opened collection called `RPC_Testing`. If something doesn't work, please let us know by creating an issue.

### Create Global and Environment Variables

Postman allows the user to specify custom `variables`, which can be used, for example, to run the same test collection against an API at multiple different endpoints. We will use `variables` for exactly this reason.

In order for this to work, we need to create both `global` and `environmental` variables. We do that next.

#### Globals

Near the top right of the screen is a small icon that looks like an eyeball and is labeled `Environment quick look`. Click on that icon and then `Edit` under the **Globals** section. You should be in the `Manage Environment` dialog.

Add two variables (you may add more later):

| VARIABLE   | INITIAL VALUE                        |
| ---------- | ------------------------------------ |
| ERIGON     | http://localhost:8545                |
| NETHERMIND | http://archive02.archivenode.io:8545 |

Click on `Persist All` and then `Save`. Close the `Manage Environment` dialog.

#### Environments

Now we need to create a testing environment. Do this by clicking on the eyeball icon again. This time, click on the `Add` link next to the **Environment** section. Call your environment `Erigon Testing` and add this variable:

| VARIABLE | INITIAL VALUE |
| -------- | ------------- |
| HOST     | {{ERIGON}}    |

`Persist All` and click on `Add` to save the environment. Close the dialog to return to the main screen.

You should be ready to test. If not, please post an issue.

#### Testing Other Endpoints

Optionally, you may create a second environment (`Nethermind Testing`) and set the `HOST` variable to `{{NETHERMIND}}`. This will allow you to test other endpoints. We leave that as an exercise.

### Running Tests

You are now ready to run the tests. To do that:

-   Start your Erigon node.
-   Start your Erigon RPC daemon. (If you're testing all endpoints, start with all namespaces enabled `build/bin/rpcdaemon --private.api.addr=localhost:9090 --http.api=eth,debug,net,web3,trace,db,shh,tg`.)
-   Click on the `Runner` button at the top left of the Postman screen. This will open a new window called `Collection Runner`.
-   Select the `RPC_Testing` collection.
-   Select an environment (`Erigon Testing` for example).
-   Press `Run RPC_Testing`

This should run all the currently enabled tests. Note that you may run individual tests directly from the Postman screen.

See the notes below for more information.

## Discussion

We think Postman is a good choice to create, edit, test, and document Erigon's RPC. The file created by Postman, `RPC_Testing.json`, is a full specification of the API including example usage, test cases, and text that may be used to generate documentation using various tools such as Swagger. Additionally, Postman allows one to create a automated monitors that watch your API. And finally, it works with your CI (continuous integration) with a tool call Newman (sp?).

## Notes:

-   The RPC_Testing file contains tests that are disabled by default. You may enable them by adding a **Global** variable called `TEST_NOT_IMPLEMENTED` and/or `TEST_DEPRECATED` and setting their value to `true`.
-   Many of the tests hard code both the body of the test and the expected results. Eventually, we'd like to use parameterized test data files instead. This will allow us to run multiple different tests against the same API endpoints.
-   The tests run against the Ethereum main net and expect an Ethereum archive node to work (for example, some of the tests query historical account balances). Future version could be customized for non-archive nodes.

## Other Possible Uses for the Collection

-   Test against other RPC endpoints (including other nodes types)
-   Generate help documentation
-   Verify RPC interfaces
-   Use in CI (continuous integration) pipeline
-   Generate RPC APIs for other languages (such as C++)
