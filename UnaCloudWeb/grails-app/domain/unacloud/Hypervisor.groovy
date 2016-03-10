package unacloud

/**
 * Entity to represent a Hypervisor.
 * This class is unused in this version of UnaCloud
 * @author CesarF
 *
 */
class Hypervisor {
	
	//-----------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------
	
	/**
	 * hypervisor name
	 */
	String name
	
	/**
	 * hypervisor version
	 */
	String hypervisorVersion
	
	/**
	 * extension from main file
	 */
	String mainExtension
	
	/**
	 * extension list of files for this hypervisor
	 * string with commas
	 * not include main
	 */
	String filesExtensions
	
    static constraints = {
		name nullable:false
		filesExtensions nullable:true
    }
}
