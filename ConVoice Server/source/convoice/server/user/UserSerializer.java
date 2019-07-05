package convoice.server.user;


// Java imports
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * The UserSerializer class is responsible for providing
 * an XML compatible interface for serializing and deserializing 
 * multiple UserData objects to XML-format. 
 */
@XmlRootElement(name = "MemberList")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserSerializer {
	/** The list of UserData objects being serialized/deserialized. */
	@XmlElement(name = "Member")
	private List<UserData> m_data = null;	
	
	/**
	 * Constructs a UserSerializer object.
	 * The object is constructed by the user code upon serialization
	 * and is used by the XML Marshaller, while upon deserialization
	 * it is constructed by the XML Unmarshaller and queried for data
	 * by the user code.
	 */
	public UserSerializer() {
		m_data = new ArrayList<UserData>();
	}
	
	/**
	 * Gets the list of UserData objects.
	 * @return The list containing the serialized/deserialized data.
	 */
	public List<UserData> getData() {
		return m_data;
	}
	
	/**
	 * Sets the list of UserData objects.
	 * @param users The list containing the serialized/deserialized data.
	 */
	public void setData(List<UserData> users) {
		m_data = users;
	}
	
};
