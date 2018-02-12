package uniandes.unacloud.web.rest.controllers

import grails.rest.RestfulController
import org.springframework.stereotype.Controller
import uniandes.unacloud.share.enums.DeploymentStateEnum
import uniandes.unacloud.share.enums.ExecutionStateEnum
import uniandes.unacloud.share.enums.ImageEnum
import uniandes.unacloud.web.domain.*
import uniandes.unacloud.web.domain.enums.ClusterEnum
import uniandes.unacloud.web.exceptions.FiveException

import uniandes.unacloud.web.exceptions.NotFoundException
import uniandes.unacloud.web.exceptions.PreconditionException
import uniandes.unacloud.web.exceptions.UnauthorizedException
import uniandes.unacloud.web.services.DeploymentService
import uniandes.unacloud.web.services.LaboratoryService
import uniandes.unacloud.web.services.UserRestrictionService
import uniandes.unacloud.web.utils.groovy.ImageRequestOptions

import javax.servlet.http.HttpServletResponse

/**
 * This Controller contains actions to manage deployment services: list, deploy and stop deployments, copy instances and add instances for deployments.
 * This class render pages for user or process request in services to update entities, there is session verification before all actions.
 * This class has the plus of being a rest controller in order to communicate itself with external apps.
 * @author s.guzmanm
 *
 */
@Controller
class DeploymentRestController extends RestfulController {

	//-----------------------------------------------------------------
	// Properties
	//-----------------------------------------------------------------

	/**
	 * Representation of cluster service
	 */
	DeploymentService deploymentService

	/**
	 * Representation of Lab service
	 */
	LaboratoryService laboratoryService

	/**
	 * Representation of User Restriction service
	 */
	UserRestrictionService userRestrictionService

	//-----------------------------------------------------------------
	// Actions MVC
	//-----------------------------------------------------------------

	/**
	 * Makes session verifications before executing any action
	 */


	def beforeInterceptor = {
        println "Enter action"
        /*
		if (!session.user) {
			flash.message = "You must log in first"
			redirect(uri:"/login", absolute:true)
			return false
		}
		def user = User.get(session.user.id)
		session.user.refresh(user)
		if (!user.status.equals(UserStateEnum.AVAILABLE)) {
			flash.message = "You don\'t have permissions to do this action"
			redirect(uri:"/", absolute:true)
			return false
		}
		*/
	}

    /**
     * Method for showing error testing in REST a way is found to instantiate images in IntelliJIDEA of Sergio´s Mac
     */
    def errorTest()
    {
        println "It ought to be deploying"
        //Testing for e
        switch (request.getHeader('exception'))
        {
            case '500': throw new FiveException("4")
            case '404': response.sendError(HttpServletResponse.SC_NOT_FOUND); throw new NotFoundException("MSJ")
            case '412':response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED); throw new PreconditionException("MSJ")
            case '401': response.sendError(HttpServletResponse.SC_UNAUTHORIZED) ;throw new UnauthorizedException("MSJ")
        }
        println "Nada"
        throw new Exception("MSJ")
    }

	/**
	 * Deploy action. Checks image number and depending on it, forms the parameters in
	 * order to pass them to the service layer. It also catches exceptions and pass
	 * them to error view. If everything works it redirects to list deployment view.
	 */

	def deploy() {

        Deployment instance=super.createResource()
        Cluster cluster = Cluster.get(instance.cluster.id)
        if (cluster) {
            //Have to define if the session arrives as a token or through what sort of media
            def user = User.get(session.user.id)
            //validates if user is owner to deploy cluster
            if (user.userClusters.find { it.id == cluster.id } != null) {
                if (cluster.state.equals(ClusterEnum.AVAILABLE)) {

                    //Validates if images are available in the platform
                    def availables = cluster.images.findAll { it.state == ImageEnum.AVAILABLE }
                    if (availables.size() != cluster.images.size()) {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        throw new PreconditionException("Some images of this cluster are not available at this moment. Please, change cluster to deploy or images in cluster.");
                    }
                    try {
                        //validates if cluster is good configured
                        def requests = new ImageRequestOptions[cluster.images.size()];
                        cluster.images.eachWithIndex { it, idx ->

                            HardwareProfile hp = HardwareProfile.get(request.getHeader('option_hw_' + it.id))
                            requests[idx] = new ImageRequestOptions(it, hp, request.getHeader('instances_' + it.id).toInteger(), request.getHeader('host_' + it.id), (request.getHeader('highAvailability_' + it.id)) != null);
                        }
                        deploymentService.deploy(cluster, user, instance.time.toLong() * 60 * 60 * 1000, requests)
                        return

                        } catch (Exception e) {
                            e.printStackTrace()
                            response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
                            throw new PreconditionException ("The request does not contain all images ",e.getCause())
                        }
                    }
                    else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND);
                        throw new NotFoundException('The cluster is not available')
                    }
            }
            else
            {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                throw new UnauthorizedException('You don\'t have permissions to deploy this cluster ')
            }
	    }
    }
	
	
	/**
	 * Deployment list action. Controls view all function 
	 * @return deployments that must be shown according to view all checkbox
	 */
	def list() {
        //Need to define authenticity of user through token or another sort of media
		def user = User.get(session.user.id)
		if (!user.isAdmin())
			[myDeployments: user.getActiveDeployments()]		
		else {	
			def deployments = deploymentService.getActiveDeployments(user)	
			[myDeployments: user.getActiveDeployments(), deployments: deployments]
		}
	}

	final preconditionException(final PreconditionException e)
	{
		println "Entró excepción"
	}

	/** Log exception */
    private void logException(final Exception exception) {
        println "ERROE-----"
        println "Exception occurred. ${exception?.class}"
    }

}
