package org.bigbluebutton.web.controllers

import org.jsecurity.authc.AuthenticationException
import org.jsecurity.authc.UsernamePasswordToken
import org.jsecurity.SecurityUtils
import org.jsecurity.session.Session
import org.jsecurity.subject.Subject
import org.springframework.util.FileCopyUtils

import grails.converters.*
import org.bigbluebutton.web.services.PresentationService

class PresentationController {
    PresentationService presentationService
    static transactional = true
    
    def index = {
    	println 'in PresentationController index'
    	render(view:'upload-file') 
    }
	
    def list = {						      				
		def f = confInfo()
		println "conference info ${f.conference} ${f.room}"
		def presentationsList = presentationService.listPresentations(f.conference, f.room)

		if (presentationsList) {
			withFormat {				
				xml {
					render(contentType:"text/xml") {
						conference(id:f.conference, room:f.room) {
							presentations {
								for (s in presentationsList) {
									presentation(name:s)
								}
							}
						}
					}
				}
			}
		} else {
			render(view:'upload-file')
		}
    }

    def delete = {		
		def filename = params.presentation_name
		def f = confInfo()
		presentationService.deletePresentation(f.conference, f.room, filename)
		flash.message = "file ${filename} removed" 
		redirect( action:list )
    }

	def upload = {		
		println 'PresentationController:upload'
		def file = request.getFile('fileUpload')
	    if(!file.empty) {
	    	flash.message = 'Your file has been uploaded'
	    	// Replace any character other than a (A-Z, a-z, 0-9, _ or .) with a - (dash).
	    	def notValiedCharsRegExp = /[^0-9a-zA-Z_\.]/
	    	def presentationName = params.presentation_name.replaceAll(notValiedCharsRegExp, '-')
	    	File uploadDir = presentationService.uploadedPresentationDirectory(params.conference, params.room, presentationName)
	    	
	    	def newFilename = file.getOriginalFilename().replaceAll(notValiedCharsRegExp, '-')
	    	def pres = new File( uploadDir.absolutePath + File.separatorChar + newFilename )
	    	file.transferTo( pres )	
	  		presentationService.processUploadedPresentation(params.conference, params.room, presentationName, pres)							             			     	
		}    
	    else {
	       flash.message = 'file cannot be empty'
	    }
		redirect( action:list)
	}

	//handle external presentation server 
	def delegate = {		
		println '\nPresentationController:delegate'
		
		def presentation_name = request.getParameter('presentation_name')
		def conference = request.getParameter('conference')
		def room = request.getParameter('room')
		def returnCode = request.getParameter('returnCode')
		def totalSlides = request.getParameter('totalSlides')
		def slidesCompleted = request.getParameter('slidesCompleted')
		
	    presentationService.processDelegatedPresentation(conference, room, presentation_name, returnCode, totalSlides, slidesCompleted)
		redirect( action:list)
	}
	
	def showSlide = {
		def presentationName = params.presentation_name
		def conf = params.conference
		def rm = params.room
		def slide = params.id
		
		InputStream is = null;
		try {
//			def f = confInfo()
			def pres = presentationService.showSlide(conf, rm, presentationName, slide)
			if (pres.exists()) {
				def bytes = pres.readBytes()
				response.addHeader("Cache-Control", "no-cache")
				response.contentType = 'application/x-shockwave-flash'
				response.outputStream << bytes;
			}	
		} catch (IOException e) {
			System.out.println("Error reading file.\n" + e.getMessage());
		}
		
		return null;
	}
	
	def showThumbnail = {
		def presentationName = params.presentation_name
		def conf = params.conference
		def rm = params.room
		def thumb = params.id
		
		InputStream is = null;
		try {
			def pres = presentationService.showThumbnail(conf, rm, presentationName, thumb)
			if (pres.exists()) {
				def bytes = pres.readBytes()
				response.addHeader("Cache-Control", "no-cache")
				response.contentType = 'image'
				response.outputStream << bytes;
			}	
		} catch (IOException e) {
			System.out.println("Error reading file.\n" + e.getMessage());
		}
		
		return null;
	}
	
	def show = {
		//def filename = params.id.replace('###', '.')
		def filename = params.presentation_name
		InputStream is = null;
		System.out.println("showing ${filename}")
		try {
			def f = confInfo()
			def pres = presentationService.showPresentation(f.conference, f.room, filename)
			if (pres.exists()) {
				System.out.println("Found ${filename}")
				def bytes = pres.readBytes()

				response.contentType = 'application/x-shockwave-flash'
				response.outputStream << bytes;
			}	
		} catch (IOException e) {
			System.out.println("Error reading file.\n" + e.getMessage());
		}
		
		return null;
	}
	
	def thumbnail = {
		def filename = params.id.replace('###', '.')
		System.out.println("showing ${filename} ${params.thumb}")
		def presDir = confDir() + File.separatorChar + filename
		try {
			def pres = presentationService.showThumbnail(presDir, params.thumb)
			if (pres.exists()) {
				def bytes = pres.readBytes()

				response.contentType = 'image'
				response.outputStream << bytes;
			}	
		} catch (IOException e) {
			System.out.println("Error reading file.\n" + e.getMessage());
		}
		
		return null;
	}

	def numberOfSlides = {
		def presentationName = params.presentation_name
		def conf = params.conference
		def rm = params.room
		
		def numThumbs = presentationService.numberOfThumbnails(conf, rm, presentationName)
			response.addHeader("Cache-Control", "no-cache")
			withFormat {						
				xml {
					render(contentType:"text/xml") {
						conference(id:conf, room:rm) {
							presentation(name:presentationName) {
								slides(count:numThumbs) {
								  for (def i = 1; i <= numThumbs; i++) {
								  	slide(number:"${i}", name:"slide/${i}", thumb:"thumbnail/${i}")
								  }
								}
							}
						}
					}
				}
			}		
	}
		
	def numberOfThumbnails = {
		def filename = params.presentation_name
		def f = confInfo()
		def numThumbs = presentationService.numberOfThumbnails(f.conference, f.room, filename)
			withFormat {				
				xml {
					render(contentType:"text/xml") {
						conference(id:f.conference, room:f.room) {
							presentation(name:filename) {
								thumbnails(count:numThumbs) {
									for (def i=0;i<numThumbs;i++) {
								  		thumb(name:"thumbnails/${i}")
								  	}
								}
							}
						}
					}
				}
			}		
	}
	
	def confInfo = {
//    	Subject currentUser = SecurityUtils.getSubject() 
//		Session session = currentUser.getSession()

	    def fname = session["fullname"]
	    def rl = session["role"]
	    def conf = session["conference"]
	    def rm = session["room"]
	    println "Conference info: ${conf} ${rm}"
		return [conference:conf, room:rm]
	}
}

