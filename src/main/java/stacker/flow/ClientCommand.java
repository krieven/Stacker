package stacker.flow;

import stacker.Command;

class ClientCommand {
    Command.Type command;
    String flow;
    String state;
    Object data;
}
