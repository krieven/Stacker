# Contracts

## Contracts basics

All contracts are based on COMMAND pattern, where the name of the command is placed in "command" field and the argument is all other fields.

Only "Client -> Router" contract have no "command" field - the command is recognized by current state of session, if no session state currently presented then the command will be "open", otherwise the command will be "action". 

## Client -> Router contract
### Request

    {
        sid: string,
        body: string?
    }
Where 
* <b>sid</b> - session id
* <b>body</b> - request body with inner structure, depends on current <b>Client -> FSM</b> contract

### Response

    {
        sid: string,
        body: string
    }
Where 
* <b>sid</b> - session id
* <b>body</b> - response body with inner structure, depends on current <b>Client -> FSM</b> contract

## Router -> FSM contract
### Request
#### When action from the client should be passed to the current FSM

    {
        command: "action",
        service: string,
        state: string,
        stateData: string?,
        body: string?
    }

#### When new FSM will be opened

    {
        command: "open",
        service: string,
        state: null,
        stateData: null,
        body: string?
    }

#### When current FSM is closed and its result is passed to caller

    {
        command: "return",
        service: string,
        state: string,
        stateData: string,
        onReturn: string,
        body: string
    }

### Response
#### When response should be sent to the client

    {
        command: "result",
        state: string,
        stateData: string,
        body: string
    }


#### When other FSM should be opened

    {
        command: "open",

        service: string,
        state: string,
        stateData: string,
        body: string?,

        onReturn: string,
    }


#### When the current FSM will be closed and control should be returned to the previous FSM

    {
        command: "return",
        body: string?
    }

#### When some Error occured

    {
        command: "error",
        body: string?
    }

## Client -> FSM contracts
Client -> FSM contracts are also based on COMMAND pattern, it is passed thru Router in the "body" field as is and will be parsed by STATE. The "body" field inner structure is explained below.  
### Request

    {
        action: string,
        data: object
    }
Where
* <b>action</b> - the name of handler that should be called, the action is case insensitive
* <b>data</b> - some kind of object, depends on contract of STATE.
### Response

    {
        service: string,
        state: string,
        data: object
    }
Where
* <b>service</b> - the name of current FSM, the name is case insensitive,
* <b>state</b> - the name of current STATE, the name is case insensitive
* <b>data</b> - some kind of object, depends on contract of  current STATE.

