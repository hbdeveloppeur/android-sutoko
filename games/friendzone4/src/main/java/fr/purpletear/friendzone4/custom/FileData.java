package fr.purpletear.friendzone4.custom;

public class FileData {
    public long id;
    public String fileName;
    public String path;
    public String dateTaken;
    public String dateAdded;

    @Override
    public String toString() {
        return "Data{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", path='" + path + '\'' +
                ", dateTaken='" + dateTaken + '\'' +
                ", dateAdded='" + dateAdded + '\'' +
                '}';
    }
}