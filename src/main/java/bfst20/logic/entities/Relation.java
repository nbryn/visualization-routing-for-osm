package bfst20.logic.entities;

import bfst20.logic.interfaces.OSMElement;

import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
//TODO: FIX
public class Relation implements OSMElement {
    private int id;
    private XMLStreamReader reader;
    private Map<String, String> tags;
    private List<Long> members;

    public Relation(){
        tags = new HashMap<>();
        members = new ArrayList<>();
    }

    public void addMember(long member){
        members.add(member);
    }

    public void addTag(String key, String value){
        tags.put(key, value);
    }

    public String getTag(String key){
        return tags.get(key);
    }

    public List<Long> getMembers(){
        return members;
    }

    public int getId() {
        return id;
    }

    @Override
    public void setReader(XMLStreamReader reader) {
        this.reader = reader;
    }

    @Override
    public void setValues() {
        
    }

}
