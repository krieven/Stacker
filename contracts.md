# Contracts

## Contracts basics

All contracts are based on COMMAND pattern, where the name of the command is placed in "command" field and the argument is all other fields.

Only "Client -> Router" outerCallContract have no "command" field - the command is recognized by current state of session, if no session state currently presented then the command will be "open", otherwise the command will be "action". 

## Client -> Router contract
### Request

    {
        sid: string,
        contentBody: string?
    }
Where 
* <b>sid</b> - session id
* <b>contentBody</b> - request contentBody with inner structure, depends on current <b>Client -> Flow</b> contract

### Response

    {
        sid: string,
        contentBody: string
    }
Where 
* <b>sid</b> - session id
* <b>contentBody</b> - response contentBody with inner structure, depends on current <b>Client -> Flow</b> outerCallContract

## Router -> Flow outerCallContract
### Request
#### When ANSWER from the client should be passed to the current Flow

    {
        type: "ANSWER",
        flow: string,
        state: string,
        flowData: string?,
        contentBody: string?
    }

#### When new Flow will be opened

    {
        type: "OPEN",
        flow: string,
        state: null,
        flowData: null,
        contentBody: string?
    }

#### When current Flow is closed and its result is passed to caller

    {
        type: "RETURN",
        flow: string,
        state: string,
        flowData: string,
        onReturn: string,
        contentBody: string
    }

### Response
#### When response should be sent to the client

    {
        type: "QUESTION",
        state: string,
        flowData: string,
        contentBody: string
    }


#### When other Flow should be opened

    {
        type: "OPEN",
        flow: string,//flow that should be opened
        state: string, //state of opener flow
        flowData: string, //updated flowData of opener flow
        contentBody: string?, //argument data
    }

The state of opener flow should be 

#### When the current Flow will be closed and control should be returned to the previous Flow

    {
        type: "RETURN",
        contentBody: string?
    }

#### When some Error occurred

    {
        type: "ERROR",
        state: string,
        flowData: string,
        contentBody: string?
    }

## Client -> Flow contracts

