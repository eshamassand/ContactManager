/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ContactManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

/**
 * Class to manage contacts and meetings
 *
 * @author Esha
 */
public class ContactManagerImpl implements ContactManager {

    private Set<Contact> contactSet;
    private List<Meeting> meetingList;
    private Calendar date;

    // Structures to store meetingIDs and contactSets
    private Map<Integer, Meeting> meetingIDMap;
    private Map<Integer, Contact> contactIDMap;
    private Map<Integer, List< Meeting>> contactIDAndMeetingList;

    public ContactManagerImpl() throws FileNotFoundException, ParseException, IOException {
        contactSet = new HashSet<Contact>();
        meetingList = new ArrayList<Meeting>();
        date = new GregorianCalendar();
        meetingIDMap = new HashMap<Integer, Meeting>(); //ids to meetings
        contactIDMap = new HashMap<Integer, Contact>(); //ids to contacts
        contactIDAndMeetingList = new HashMap<Integer, List<Meeting>>(); //links a contact ID to a list of meetings.

        //bufferedReader must be called from within try/catch statement - to catch any IOException
        File path = new File("contacts.txt");
        File contactFile = new File(path.getAbsolutePath());
        if (contactFile.exists()) {
            System.out.println("Contacts.txt file found. \nReading from and writing to this file ...");
            checkIfFileExists();
        }
        if (!contactFile.exists()) {
            System.out.println("Contacts.txt file not found. \nCreating a new 'contacts.txt' file ...");

        }
    }

    public void checkIfFileExists() throws ParseException, IOException {
        //buffered reader to read the file
        FileReader file = null;
        BufferedReader buffer = null;
        try {
            File path = new File("contacts.txt");
            File contactFile = new File(path.getAbsolutePath());
            file = new FileReader(contactFile);
            buffer = new BufferedReader(file);

            //set booleans for processing
            boolean contacts = false;
            boolean meetings = false;

            String line;
            while ((line = buffer.readLine()) != null) {
                String[] lineItemsArray = line.split(",");

                if (lineItemsArray[0].equals("CONTACT ID NUMBER")) {
                    contacts = true;
                    meetings = false;
                } else if (lineItemsArray[0].equals("MEETING ID NUMBER")) {
                    contacts = false;
                    meetings = true;
                } else {
                    if ((contacts == true) && (meetings == false)) {
                        int contactID = (Integer.parseInt(lineItemsArray[0]));
                        String contactName = lineItemsArray[1];
                        String contactNotes = lineItemsArray[2];

                        Contact thisContact = new ContactImpl(contactID, contactName, contactNotes);
                        contactSet.add(thisContact);
                        contactIDMap.put(thisContact.getId(), thisContact);

                    }
                    if ((meetings == true) && (contacts == false)) {
                        Set<Contact> meetingContacts;
                        int meetingID = Integer.parseInt(lineItemsArray[0]);

                        //parse and construct calendar object
                        DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss z YYYY");
                        Date date = format.parse(lineItemsArray[1]);

                        Calendar calDate = new GregorianCalendar();
                        calDate.setTime(date);

                        String thisMeetingContacts = lineItemsArray[2];
                        String[] MeetingContacts = thisMeetingContacts.split(";");

                        int[] MeetingContactsIds = new int[MeetingContacts.length];

                        for (int i = 0; i < MeetingContactsIds.length; i++) {
                            MeetingContactsIds[i] = Integer.parseInt(MeetingContacts[i].trim());
                        }
                        meetingContacts = getContacts(MeetingContactsIds);
                        Calendar dateNow = new GregorianCalendar();

                        if (calDate.after(dateNow)) {
                            //construct FutureMeetingImpl without notes
                            Meeting futureMeeting = new FutureMeetingImpl(meetingID, meetingContacts, calDate);
                            meetingList.add(futureMeeting);
                            meetingIDMap.put(meetingID, futureMeeting);
                            addListOfMeetingsToContact(futureMeeting);
                        }

                        String meetingNotes = "";

                        if (calDate.before(dateNow)) {

                            if (lineItemsArray.length == 3) {
                                meetingNotes = "";
                            }
                            if (lineItemsArray.length == 4) {
                                meetingNotes = lineItemsArray[3];
                            }

                            Meeting pastMeeting = new PastMeetingImpl(meetingID, meetingContacts, calDate, meetingNotes);
                            meetingList.add(pastMeeting);
                            meetingIDMap.put(meetingID, pastMeeting);
                            addListOfMeetingsToContact(pastMeeting);
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                //must close this once complete
                file.close();
                buffer.close();
            } catch (IOException ex) {
                System.out.println("I/O exception. Buffer or File may have been 'null'.");
                ex.printStackTrace();
            }
        }
    }

    /**
     * adds a meeting to the meetingList, meetingIDMap and List of Meetings and
     * contacts (contactIDAndMeetingList)
     *
     * @param m meeting the meeting to add
     */
    public void addToMeetingStructures(Meeting m) {
        meetingList.add(m);
        meetingIDMap.put(m.getId(), m);
        addListOfMeetingsToContact(m);
    }

    /**
     * adds a contact to the contactSet, contctIDMap and List of Meetings and
     * contacts (contactIDAndMeetingList)
     *
     * @param c contact to add
     */
    public void addContactToStructures(Contact c) {
        contactSet.add(c);
        contactIDMap.put(c.getId(), c);
    }

    /**
     * adds a meeting to a contact's list of meetings, if it has not already
     * been added..
     *
     * @param m the meeting to add
     *
     */
    public void addListOfMeetingsToContact(Meeting m) {
        List<Meeting> thisList = new ArrayList<Meeting>();
        Set<Contact> mContacts = m.getContacts();
        Iterator<Contact> eachContact = mContacts.iterator();

        //only add meeting if the id is not already added. 
        //At this stage there should not be any duplicates.
        if (!contactIDAndMeetingList.containsKey(m.getId())) {

            while (eachContact.hasNext()) {
                Contact thisContact = eachContact.next();

                if (contactIDAndMeetingList.containsKey(thisContact.getId())) {
                    thisList = contactIDAndMeetingList.get(thisContact.getId());

                    thisList.add(m);
                    contactIDAndMeetingList.put(thisContact.getId(), thisList);
                }
            }
        }
        contactIDAndMeetingList.put(m.getId(), thisList); //link contact id to updatedmeeting list

    }

    /**
     * Removes a meeting from a contact's list of meetings.
     *
     * @param m the meeting to add
     *
     */
    public void removeFromListOfMeetingsForContact(Meeting m) {
        List<Meeting> thisList = new ArrayList<Meeting>();
        Set<Contact> mContacts = m.getContacts();
        Iterator<Contact> eachContact = mContacts.iterator();

        while (eachContact.hasNext()) {
            Contact thisContact = eachContact.next();

            if (contactIDAndMeetingList.containsKey(thisContact.getId())) {
                thisList = contactIDAndMeetingList.get(thisContact.getId());

                thisList.remove(m);
            }
        }
        contactIDAndMeetingList.put(m.getId(), thisList); //link contact id to updatedmeeting list

    }

    /**
     * Add a new meeting to be held in the future.
     *
     * @param contacts a list of contacts that will participate in the meeting
     * @param date the date on which the meeting will take place
     * @return the ID for the meeting
     * @throws IllegalArgumentException if the meeting is set for a time in the
     * past, of if any contact is unknown / non-existent
     *
     */
    public int addFutureMeeting(Set<Contact> contacts, Calendar date) {
        Meeting futureMeeting;
        int generatedID;

        checkArgumentIsNotNull(contacts);
        //check that the meeting is actually a future meeting (i.e., that time is valid). Use calendar class to validate the date
        this.date = new GregorianCalendar();
        if (date.before(this.date)) {
            throw new IllegalArgumentException("You entered a date in the past. Please try again: ");
        }
        //go through the entire Set of contacts and check that each and every one of them exists
        if (!contactSet.containsAll(contacts)) {
            throw new IllegalArgumentException("Unknown/non-existant contacts. ");
        }

        boolean generatedIDIsTaken = true;

        do {
            generatedID = getRandomID();
            if (!meetingIDMap.containsKey(generatedID)) {
                generatedIDIsTaken = false;
            }
        } while (generatedIDIsTaken);

        // constructor, after all checks, create a future meeting.
        futureMeeting = new FutureMeetingImpl(generatedID, contacts, date);
        //add meeting to list of meetings
        addToMeetingStructures(futureMeeting);

        return generatedID;
    }

    /**
     * Add a new meeting to be held in the future.
     *
     * @param contacts a list of contacts that will participate in the meeting
     * @param date the date on which the meeting will take place
     * @return the ID for the meeting
     *
     */
    //for purposes of testing in the main method only
    public int addFutureMeeting(int id, Set<Contact> contacts, Calendar date) {
        Meeting futureMeeting = new FutureMeetingImpl(id, contacts, date);
        //add meeting to list of meetings
        addToMeetingStructures(futureMeeting);
        return id;
    }

    /**
     * returns a random positive number to be assigned to an id
     *
     * @return id the number that will be returned
     */
    public int getRandomID() {
        Random random = new Random();
        int generatedID = random.nextInt(Integer.MAX_VALUE);
        return generatedID;
    }

    /**
     * Returns the PAST meeting with the requested ID, or null if it there is
     * none.
     *
     * @param id the ID for the meeting
     * @return the meeting with the requested ID, or null if it there is none.
     * @throws IllegalArgumentException if there is a meeting with that ID
     * happening in the future
     */
    public PastMeeting getPastMeeting(int id) {
        PastMeeting result = null;
        //if there are no meetings in the list.
        if (meetingList.isEmpty()) {
            System.out.println("The meeting list is currently empty.");
            return null;
        }
        //throw exception if a future meeting contains that id number
        Meeting meeting = meetingIDMap.get(id);
        if (meeting instanceof FutureMeeting) {
            throw new IllegalArgumentException("The id is already used for a future meeting.");
        } else if (meeting instanceof PastMeeting) {
            result = (PastMeetingImpl) meeting;
            System.out.println("Retrieving past meeting..");
        }
        return result;
    }

    /**
     * Returns the FUTURE meeting with the requested ID, or null if there is
     * none.
     *
     * @param id the ID for the meeting
     * @return the meeting with the requested ID, or null if it there is none.
     * @throws IllegalArgumentException if there is a meeting with that ID
     * happening in the past
     */
    public FutureMeeting getFutureMeeting(int id) {
        FutureMeeting result = null;
        //if there are no meetings in the list.
        if (meetingList.isEmpty()) {
            System.out.println("The meeting list is currently empty.");
            return null;
        }
        Meeting meeting = meetingIDMap.get(id);
        if (meetingIDMap.containsKey(id)) {
            System.out.println("Found meeting: " + id);
            if (meeting instanceof PastMeeting) {
                throw new IllegalArgumentException("The id is already used for a past meeting.");
            } else if (meeting instanceof FutureMeeting) {
                result = (FutureMeetingImpl) meeting;
                System.out.println("Retrieving future meeting..");
            }
        }
        return result;
    }

    /**
     * Returns the meeting with the requested ID, or null if it there is none.
     *
     * @param id the ID for the meeting
     * @return the meeting with the requested ID, or null if it there is none.
     */
    public Meeting getMeeting(int id) {
        Meeting result = null;
        //if there are no meetings in the list.
        if (meetingList.isEmpty()) {
            System.out.println("The meeting list is currently empty.");
            return null;
        } else {
            //return the meeting with the given id
            if (meetingIDMap.containsKey(id)) {
                result = meetingIDMap.get(id);
            }
        }
        return result;
    }

    /**
     * Returns the list of future meetings scheduled with this contact.
     *
     * If there are none, the returned list will be empty. Otherwise, the list
     * will be chronologically sorted and will not contain any duplicates.
     *
     * @param contact one of the user’s contacts
     * @return the list of future meeting(s) scheduled with this contact (maybe
     * empty).
     * @throws IllegalArgumentException if the contact does not exist
     */
    public List<Meeting> getFutureMeetingList(Contact contact) {
        Calendar dateToday = new GregorianCalendar();
        //create a list of future meetings to return
        List<Meeting> listOfFutureMeetings = new ArrayList<Meeting>();
        //check that the contact exists
        if (!contactSet.contains(contact)) {
            throw new IllegalArgumentException("This contact does not exist. ");
        }
        if (contactIDAndMeetingList.containsKey(contact.getId())) {
            List<Meeting> contactMeetings = null;
            contactMeetings = contactIDAndMeetingList.get(contact.getId());
            for (int i = 0; i < contactMeetings.size(); i++) {
                if (contactMeetings.get(i).getDate().after(dateToday)) {
                    listOfFutureMeetings.add(contactMeetings.get(i));
                }
            }
        }
        SortDate sortDate = new SortDate();
        Collections.sort(listOfFutureMeetings, sortDate);
        return listOfFutureMeetings;
    }

    /**
     * Returns the list of meetings that are scheduled for, or that took place
     * on, the specified date
     *
     * If there are none, the returned list will be empty. Otherwise, the list
     * will be chronologically sorted and will not contain any duplicates.
     *
     * @param date the date
     * @return the list of meetings
     */
    public List<Meeting> getFutureMeetingList(Calendar date) {
        //create a list of future meetings to return
        List<Meeting> listOfFutureMeetings = new ArrayList<Meeting>();

        /* Now get the list of meetings and search within each meeting (at index 'i') for the contacts of those individual meetings.
         *  If the contacts of those meetings contains the searched for contact, add the meeting to the list of future meetings for that contact.
         */
        for (int i = 0; i < meetingList.size(); i++) {
            if (meetingList.get(i).getDate().equals(date)) {
                listOfFutureMeetings.add(meetingList.get(i));
            }
        }
        // else, if no meetings scheduled -- will return null.
        SortDate sortDate = new SortDate();
        Collections.sort(listOfFutureMeetings, sortDate);
        return listOfFutureMeetings;
    }

    /**
     * Returns the list of past meetings in which this contact has participated.
     *
     * If there are none, the returned list will be empty. Otherwise, the list
     * will be chronologically sorted and will not contain any duplicates.
     *
     * @param contact one of the user’s contacts
     * @return the list of past meeting(s) scheduled with this contact (maybe
     * empty).
     * @throws IllegalArgumentException if the contact does not exist
     */
    public List<PastMeeting> getPastMeetingList(Contact contact) {
        Calendar dateToday = new GregorianCalendar();
        //create a list of past meetings to return
        List<PastMeeting> listOfPastMeetings = new ArrayList<PastMeeting>();

        //check that the contact exists
        if (!contactSet.contains(contact)) {
            throw new IllegalArgumentException("This contact does not exist. ");
        }
        /* Search within map for each previous meetings with contact
         *  If the contacts of those meetings contains the searched for contact, add the meeting to the list of past meetings for that contact.
         */
        if (contactIDAndMeetingList.containsKey(contact.getId())) {
            List<Meeting> contactMeetings = null;
            contactMeetings = contactIDAndMeetingList.get(contact.getId());
            for (int i = 0; i < contactMeetings.size(); i++) {
                if (contactMeetings.get(i).getDate().before(dateToday)) {
                    PastMeeting toAdd = (PastMeeting) contactMeetings.get(i);
                    listOfPastMeetings.add(toAdd);
                }
            }
        }
        SortDate sortDate = new SortDate();
        Collections.sort(listOfPastMeetings, sortDate);
        return listOfPastMeetings;
    }

    /**
     * Create a new record for a meeting that took place in the past.
     *
     * @param contacts a list of participants
     * @param date the date on which the meeting took place
     * @param text messages to be added about the meeting.
     * @throws IllegalArgumentException if the list of contacts is empty, or any
     * of the contacts does not exist
     * @throws NullPointerException if any of the arguments is null
     */
    public void addNewPastMeeting(Set<Contact> contacts, Calendar date, String text) {
        //make a new pastMeeting id number
        int generatePastMeetingID;
        checkArgumentIsNotNull(contacts);
        checkArgumentIsNotNull(date);
        checkArgumentIsNotNull(text);

        //go through the entire Set of contacts and check that each and every one of them exists
        if (!contactSet.containsAll(contacts)) {
            throw new IllegalArgumentException("One (possibly more) of the contacts entered does not exist.");
        }

        this.date = new GregorianCalendar();
        if (this.date.before(date)) {
            System.out.println("You need to enter a time in the past to add a new past meeting.");
            throw new IllegalArgumentException("Try again: ");
        }

        //create boolean to check the meetingID does not exist.
        boolean generatedPastMeetingIDTaken = true;
        do {
            generatePastMeetingID = getRandomID();

            if (!contactIDAndMeetingList.containsKey(generatePastMeetingID)) {
                generatedPastMeetingIDTaken = false;
            }
        } while (generatedPastMeetingIDTaken);

        if (!generatedPastMeetingIDTaken) {
            //contruct a new past meeting with the ID, contacts, date and notes
            Meeting pastMeeting = new PastMeetingImpl(generatePastMeetingID, contacts, date, text);
            //add meeting to the meeting list.
            addToMeetingStructures(pastMeeting);
        }
    }

    /**
     * Create a new record for a meeting that took place in the past.
     *
     * @param contacts a list of participants
     * @param date the date on which the meeting took place
     * @param text messages to be added about the meeting.
     */
    //For testing addNewPastMeeting() only. 
    public void addNewPastMeeting(int ID, Set<Contact> contacts, Calendar date, String text) {
        Meeting pastMeeting = new PastMeetingImpl(ID, contacts, date, text);
        //add meeting to the meeting list.
        addToMeetingStructures(pastMeeting);
    }

    /**
     * Create a new record for a meeting that took place in the past.
     *
     * @param contacts a list of participants
     * @throws NullPointerException if any of the arguments is null
     */
    public void checkArgumentIsNotNull(Set<Contact> contacts) {
        if (contacts == null) {
            throw new NullPointerException("Please enter the contacts. ");
        }
    }

    /**
     * Create a new record for a meeting that took place in the past.
     *
     * @param date a date of meeting
     * @throws NullPointerException if any of the arguments is null
     */
    public void checkArgumentIsNotNull(Calendar date) {
        if (date == null) {
            throw new NullPointerException("Please enter a date for the meeting.");
        }
    }

    /**
     * Create a new record for a meeting that took place in the past.
     *
     * @param text a note from a meeting, or note for a contact.
     * @throws NullPointerException if any of the arguments is null
     */
    public void checkArgumentIsNotNull(String text) {
        if (text == null) {
            throw new NullPointerException("Please enter a note for the meeting: ");
        }
    }

    /**
     * Add notes to a meeting.
     *
     * This method is used when a future meeting takes place, and is then
     * converted to a past meeting (with notes).
     *
     * It can be also used to add notes to a past meeting at a later date.
     *
     * @param id the ID of the meeting
     * @param text messages to be added about the meeting.
     * @throws IllegalArgumentException if the meeting does not exist
     * @throws IllegalStateException if the meeting is set for a date in the
     * future
     * @throws NullPointerException if the notes are null
     */
    public void addMeetingNotes(int id, String text) {
        //check meeitng exists
        for (int i = 0; i < meetingList.size(); i++) {
            if (meetingList.get(i).getId() == id) {
                System.out.println("Found the meeting : " + id);
            } else {
                throw new IllegalArgumentException("That meeting ID does not exist. ");
            }
        }
        //get current date to compare it to meeting date
        Calendar dateNow = new GregorianCalendar();

        //check there are notes to add.
        if (text == null) {
            throw new NullPointerException("Please enter a note for the past meeting: ");
        }
        //Find meeting id
        Meeting meetingToAddNotes = meetingList.get(id);
        if (meetingToAddNotes.getDate().after(dateNow)) {
            throw new IllegalStateException("That meeting is set for a date in the future "
                    + "so cannot be converted into a past meeting, and you cannot add notes yet. ");
        }
        //get all info for the meeting
        int meetingID = meetingToAddNotes.getId();
        Set<Contact> meetingContacts = meetingToAddNotes.getContacts();
        Calendar meetingDate = meetingToAddNotes.getDate();
        String meetingNotes = text;

        //remove meeting from the list
        for (int i = 0; i < meetingList.size(); i++) {
            Meeting m = meetingList.get(i);
            if (m.getId() == id) {
                meetingList.remove(m);
            }
        }

        //remove meeting from the map
        if (meetingIDMap.containsKey(id)) {
            meetingIDMap.remove(id);
        }

        //contruct the past meeting
        Meeting pMeeting = new PastMeetingImpl(meetingID, meetingContacts, meetingDate, meetingNotes);
        meetingList.add(pMeeting);
        meetingIDMap.put(id, pMeeting);
    }

    /**
     * Create a new contact with the specified name and notes.
     *
     * @param contactID the id number of the contact.
     * @param name the name of the contact.
     * @param notes notes to be added about the contact.
     * @return a new Contact object
     */
    //For testing addNewContact() method in main script only.
    public Contact addNewContact(int contactID, String name, String notes) {
        Contact newContact = new ContactImpl(contactID, name, notes);
        addContactToStructures(newContact);
        return newContact;
    }

    /**
     * Create a new contact with the specified name and notes.
     *
     * @param name the name of the contact.
     * @param notes notes to be added about the contact.
     * @throws NullPointerException if the name or the notes are null
     */
    public void addNewContact(String name, String notes) {
        int contactID;

        checkArgumentIsNotNull(name);
        checkArgumentIsNotNull(notes);

        boolean contactIdIsTaken = true;
        do {
            contactID = getRandomID();

            for (Contact c : contactSet) {
                if (c.getId() != contactID) {
                    contactIdIsTaken = false;
                }
            }
        } while (contactIdIsTaken);

        Contact newContact = new ContactImpl(contactID, name, notes);
        addContactToStructures(newContact);
    }

    /**
     * Returns a list containing the contacts that correspond to the IDs.
     *
     * @param ids an arbitrary number of contact IDs
     * @return a list containing the contacts that correspond to the IDs.
     * @throws IllegalArgumentException if any of the IDs does not correspond to
     * a real contact
     */
    public Set<Contact> getContacts(int... ids) {
        Set<Contact> theseContacts = new HashSet<Contact>();

        //check that all the id's entered exist. 
        for (int i = 0; i < ids.length; i++) {
            if (contactIDMap.containsKey(ids[i])) {
                Contact toAdd = contactIDMap.get(ids[i]);
                theseContacts.add(toAdd);
            } else {
                throw new IllegalArgumentException("The id: " + ids[i] + " does not correspond to a real contact. ");
            }
        }
        return theseContacts;
    }

    /**
     * Returns a list with the contacts whose name contains that string.
     *
     * @param name the string to search for
     * @return a list with the contacts whose name contains that string.
     * @throws NullPointerException if the parameter is null
     */
    public Set<Contact> getContacts(String name) {
        checkArgumentIsNotNull(name);
        Set<Contact> theseContacts = new HashSet<Contact>();;
        for (Contact c : contactSet) {
            if (c.getName().equals(name)) {
                theseContacts.add(c);
            }
            if (theseContacts.isEmpty()) {
                throw new NullPointerException("There are no contacts with that name.");
            }
        }
        return theseContacts;
    }

    /**
     * Saves all data to disk.
     *
     * This method must be executed when the program is closed and when/if the
     * user requests it.
     */
    public void flush() {
        FileWriter fileWrite = null;
        boolean createNewFile = true;
        try {
            File contactFile = new File("contacts.txt");
            if (!contactFile.exists()) {
                createNewFile = contactFile.createNewFile();
            }
            if (createNewFile) {
                fileWrite = new FileWriter("contacts.txt");
            }

            //write to file the contacts and meetings
            //contacts have an ID a Name and Notes and are stored in the contactSet.
            //Write headers:
            fileWrite.write("CONTACT ID NUMBER,CONTACT NAME,CONTACT NOTES,");

            if (contactSet == null) {
                throw new NullPointerException("Your contact list is empty.");
            }

            String contactDataEntry = "";
            for (Contact c : contactSet) {
                contactDataEntry = c.getId() + "," + c.getName() + "," + c.getNotes();
                fileWrite.write("\n" + contactDataEntry);
            }
            fileWrite.write("\n");

            //meetings have an ID a Date and Contacts. Notes are associated with PastMeeting. Meetings are stored in the meetingList.
            //Write headers:
            fileWrite.write("MEETING ID NUMBER,MEETING DATE,MEETING ATTENDEE (ID NUMBERS) LIST,MEETING NOTES");
            if (meetingList == null) {
                throw new NullPointerException("Your contact list is empty.");
            }
            String meetingDataEntry = "";//StringBuffer() does not work when I parse the contacts and meetings from the contacts.txt file at startup.
            for (Meeting m : meetingList) {
                meetingDataEntry = meetingDataEntry + "\n" + m.getId() + "," + m.getDate().getTime() + ",";

                Object[] contactListForDataEntry;
                Object thisContact;
                //Get contactSet and convert to Array, and String to print in contacts.txt. 
                Set<Contact> workingContacts = m.getContacts();
                contactListForDataEntry = workingContacts.toArray();

                for (int i = 0; i < contactListForDataEntry.length; i++) {
                    thisContact = contactListForDataEntry[i];
                    Contact c = (Contact) thisContact;

                    meetingDataEntry = meetingDataEntry + c.getId() + ";";

                }

                if (m instanceof PastMeeting) {
                    PastMeeting pMeeting = (PastMeeting) m;
                    String notes = pMeeting.getNotes();

                    if (notes == null) {
                        notes = "";
                    }

                    meetingDataEntry = meetingDataEntry + "," + notes;
                }
                //do nothing if FutureMeeting
            }

            fileWrite.write(meetingDataEntry + "");
        } catch (IOException e) {
            System.out.println("An error has occurred");
        } finally {
            try {
                fileWrite.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /* Method to search for and print out the name and id number of the contact provided within the contactSet to search.
     * @param nameOfContactSetToSearch, the contactSet to search for the name
     * @param name, contact name
     */
    //For testing purposes in a main() script.
    public void getContactIdFromSet(Set<Contact> nameOfContactSetToSearch, Contact name) {
        try {
            if (nameOfContactSetToSearch.contains(name)) {
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }//did not use system.exit(0); because it would exit whenever the user flushed(), which could be an intermittent save rather than an exit.
    }

    /* Getter for this.contactSet
     * @return this.contactSet
     */
    public Set<Contact> getContactSet() {
        return this.contactSet;
    }

    /* Getter for this.meetingList
     * @return this.meetingList
     */
    public List<Meeting> getMeetingList() {
        return this.meetingList;
    }

    /* Getter for this.meetingIDMap
     * @return this.meetingIDMap
     */
    public Map<Integer, Meeting> getMeetingMap() {
        return this.meetingIDMap;
    }

    /* Getter for this.contactIDMap
     * @return this.contactIDMap
     */
    public Map<Integer, Contact> getContactMap() {
        return this.contactIDMap;
    }

}
