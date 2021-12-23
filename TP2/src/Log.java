
import java.util.logging.*;

import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.stream.Collectors;
import java.util.stream.Stream;



  public class Log {
      private final static  Logger logr = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
      public  static Logger start(){
            LogManager.getLogManager().reset();
            logr.setLevel(Level.ALL);

            try{
                FileHandler fh= new FileHandler("myLogger.log");
                fh.setLevel(Level.ALL);
                logr.addHandler(fh);

            }catch (IOException e){
                logr.log(Level.SEVERE,"File logger not working", e);

            }
          /*
            SEVERE
            WARNING
            INFO
            CONFIG
            FINE
            FINER
            FINEST
             */
        return logr;
      }}

