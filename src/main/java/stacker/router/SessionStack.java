package stacker.router;

import java.util.ArrayList;

public class SessionStack extends ArrayList<SessionStackEntry> {

    private static final long serialVersionUID = 1L;

    public SessionStackEntry pop(){
        return this.remove(0);
    }
    public void push(SessionStackEntry entry){
        this.add(0, entry);
    }

    public SessionStackEntry getCurrent(){
        return this.get(0);
    }
}