path = File.expand_path(File.join(File.dirname(__FILE__), '../lib'))
$LOAD_PATH << path
require 'recordandplayback/archiver'
require 'recordandplayback/audio_archiver'
require 'recordandplayback/collectors/events'
require 'recordandplayback/collectors/audio'
require 'recordandplayback/generators/events'
require 'recordandplayback/generators/audio'
require 'recordandplayback/generators/matterhorn_processor'
require 'recordandplayback/generators/audio_processor'
require 'recordandplayback/generators/deskshare'

module BigBlueButton
  class MissingDirectoryException < Exception
  end
  
  class FileNotFoundException < Exception
  end
  
  # BigBlueButton logs information about its progress.
  # Replace with your own logger if you desire.
  #
  # @param [Logger] log your own logger
  # @return [Logger] the logger you set
  def self.logger=(log)
    @logger = log
  end
  
  # Get BigBlueButton logger.
  #
  # @return [Logger]
  def self.logger
    return @logger if @logger
    logger = Logger.new(STDOUT)
    logger.level = Logger::INFO
    @logger = logger
  end
  
  def self.dir_exists?(dir)
    FileTest.directory?(dir)
  end
  
  
  def self.execute(command)
    Open3.popen3(command) do | stdin, stdout, stderr|
        BigBlueButton.logger.info("Executing: #{command}")
        errors = stderr.readlines
        BigBlueButton.logger.info( "Output: #{stdout.readlines} ")
        BigBlueButton.logger.info( "Error: stderr: #{errors}"); raise errors.to_s unless errors.empty?
    end
  end

  module Archive
    # BigBlueButton logs information about its progress.
    # Replace with your own logger if you desire.
    #
    # @param [Logger] log your own logger
    # @return [Logger] the logger you set
    def self.logger=(log)
      @logger = log
    end
    
    # Get BigBlueButton logger.
    #
    # @return [Logger]
    def self.logger
      return @logger if @logger
      logger = Logger.new(STDOUT)
      logger.level = Logger::INFO
      @logger = logger
    end  
  end
  
  module Process
    # BigBlueButton logs information about its progress.
    # Replace with your own logger if you desire.
    #
    # @param [Logger] log your own logger
    # @return [Logger] the logger you set
    def self.logger=(log)
      @logger = log
    end
    
    # Get BigBlueButton logger.
    #
    # @return [Logger]
    def self.logger
      return @logger if @logger
      logger = Logger.new(STDOUT)
      logger.level = Logger::INFO
      @logger = logger
    end    
  end
  
  
end