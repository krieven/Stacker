package stacker.machine;

public interface IBody<DataT>{
    String getAction();
    void setAction(String action);
    DataT getData();
    void setData(DataT data);
}