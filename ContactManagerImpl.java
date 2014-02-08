/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ContactManager;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;//may need this import.
/**
 * Class to manage contacts and meetings
 * @author Esha
 */
public class ContactManagerImpl {//implements ContactManager {
    private Set<Contact> contacts;
    private Calendar date; 
    private int id;
    private String text;//notes about meeting
    
    
    /**
    * Add a new meeting to be held in the future.
    *
    * @param contacts a list of contacts that will participate in the meeting
    * @param date the date on which the meeting will take place
    * @return the ID for the meeting
    * @throws IllegalArgumentException if the meeting is set for a time in the past,
    * of if any contact is unknown / non-existent
    */
    public int addFutureMeeting(Set<Contact> contacts, Calendar date){
        //check that the meeting is actually a future meeting (i.e., that time is valid). Use calendar class to validate the date
        try{
            if (date.getTime().before(this.date.getTime())){
            System.out.println("Please enter a date in the Future");
            }
        } catch (IllegalArgumentException e){
            System.out.println("Try again: ");
        }
        // constructor
        Meeting futureMeeting = new FutureMeetingImpl(id, contacts, date);
        return futureMeeting.getId();
    }
    
    public PastMeeting getPastMeeting(int id){
        
    }
    
    public FutureMeeting getFutureMeeting(int id){
        
    }
    
    public Meeting getMeeting(int id){
        
    }
    
    public List<Meeting> getFutureMeetingList(Contact contact){
        
    }
    
    public List<Meeting> getFutureMeetingList(Calendar date){
        
    }
    
    public List<PastMeeting> getPastMeetingList(Contact contact){
        
    }
    
    public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text){
        
    }
    
    public void addMeetingNotes(int id, String text){
        
    }
    
    public void addNewContact(String name, String notes){
        
    }
    
    public Set<Contact> getContacts(int... ids){
        
    }
    
    public Set<Contact> getContacts(String name){
        
    }
    
    public void flush(){
        
    }
    
    
}
