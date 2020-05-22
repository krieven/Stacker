package stacker.service;

import stacker.Command;

class ClientCommand {
    Command.Type command;
    String service;
    String state;
    Object data;
}
