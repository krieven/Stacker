# Contracts

## Contracts basics

All contracts are based on COMMAND pattern, where the name of the command is placed in "command" field and the argument is all other fields.

Only "Client -> Router" outerCallContract have no "command" field - the command is recognized by current state of session, if no session state currently presented then the command will be "open", otherwise the command will be "action". 

## Client -> Router outerCallContract
### Request

    {
        sid: string,
        body: string?
    }
Where 
* <b>sid</b> - session id
* <b>body</b> - request body with inner structure, depends on current <b>Client -> Flow</b> outerCallContract

### Response

    {
        sid: string,
        body: string
    }
Where 
* <b>sid</b> - session id
* <b>body</b> - response body with inner structure, depends on current <b>Client -> Flow</b> outerCallContract

## Router -> Flow outerCallContract
### Request
#### When action from the client should be passed to the current Flow

    {
        command: "ACTION",
        flow: string,
        state: string,
        flowData: string?,
        body: string?
    }

#### When new Flow will be opened

    {
        command: "OPEN",
        flow: string,
        state: null,
        flowData: null,
        body: string?
    }

#### When current Flow is closed and its result is passed to caller

    {
        command: "RETURN",
        flow: string,
        state: string,
        flowData: string,
        onReturn: string,
        body: string
    }

### Response
#### When response should be sent to the client

    {
        command: "RESULT",
        state: string,
        flowData: string,
        body: string
    }


#### When other Flow should be opened

    {
        command: "OPEN",

        flow: string,//flow that should be opened
        state: string, //state of opener flow
        flowData: string, //updated flowData of opener flow
        body: string?, //argument data

        onReturn: string
    }


#### When the current Flow will be closed and control should be returned to the previous Flow

    {
        command: "RETURN",
        body: string?
    }

#### When some Error occurred

    {
        command: "ERROR",
        state: string,
        flowData: string,
        body: string?
    }

## Client -> Flow contracts
Client -> Flow contracts are also based on COMMAND pattern, it is passed thru Router in the "body" field as is and will be parsed by STATE. The "body" field inner structure is explained below.
### Request

    {
        action: string,
        data: object
    }
Where
* <b>action</b> - the name of handler that should be called, the action is case insensitive
* <b>data</b> - some kind of object, depends on outerCallContract of STATE.
### Response

    {
        command: "RESULT" || "ERROR"
        flow: string,
        state: string,
        data: object
    }
Where
* <b>flow</b> - the name of current Flow, the name is case insensitive,
* <b>state</b> - the name of current STATE, the name is case insensitive
* <b>data</b> - some kind of object, depends on outerCallContract of  current STATE.

