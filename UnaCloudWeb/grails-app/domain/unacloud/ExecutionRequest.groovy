package unacloud

import java.util.Date;

import unacloud.share.enums.VirtualMachineExecutionStateEnum;

/**
 * Entity to represent the state of execution in an specified time.
 * The purpose of this class is analyze the machine state in range time.
 * This entity is create by triggers in database (Check DatabaseService)
 *
 * @author CesarF
 */
class ExecutionRequest {
	
	/**
	 * Node state (QUEUED,COPYING,CONFIGURING,DEPLOYING,DEPLOYED,FAILED,FINISHING,FINISHED,REQUEST_COPY,RECONNECTING)
	 */
	VirtualMachineExecutionStateEnum status
	
	/**
	 * Date when the node change of status
	 */
	Date requestTime
	
	/**
	 * Execution which belongs this request
	 */
	static belongsTo=  [execution:VirtualMachineExecution]

	/**
	 * status and time never can be null
	 */
    static constraints = {
		status nullable:false
		requestTime nullable: false
    }
}
