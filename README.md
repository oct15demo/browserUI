# browserUI
All product and company names are trademarks™ or registered® trademarks of their respective holders. 
Use of name the Knime does not imply any affiliation with or endorsement by the company.   

The following instructions summarize the directions from the Knime website for setting 
up the Knime source code in Eclipse 4.6.1 for examination or modification of the code. 
The Knime version is 3.4.2, and please note that Knime warns against using Eclipse 4.6.2 
due to problems compiling with that version. These files and setup were assembled 
mid October 2017.  

The purpose of this project is to expose a version of Knime that demonstrates a 
browser workflow UI. Workflow creation and execution is to be made accessible through 
any web browser, the engineered to leverage existing code. 
The goal is to create a working prototype with as little code modification as possible, 
so later expanding code to encompass more full functionality can be done in the most efficient manner.  

The main components of the Knime application, and this setup, are org.knime.base and org.knime.core. 
Additionally, org.knime.js.core and org.knime.js.views are incorporated into the base project
as a convenient shortcut (or hack) instead being set up as thier own projects. They are needed to 
display the output of workflows in the browser. js.views is a component of Knime Labs, meaning it is 
an additional package not yet included in the standard distribution.  

The setup described below installs a new clean Knime codebase of the source code needing modification 
to create a browser UI version. 

The additional code and modifications, already written to create an early version of the browser UI, will 
be added to this repository soon.  The Java code is currently running a demo located in the links given below. 
The modifications and additions to code of that demo are temporarily unavailable, awaiting completion of an
extensive refactoring task.  The code was originally produced in a very experimental manner, likewise the 
javascript used in the POC is also currently being refactored, to be added when done.

The simplified working version of the browser UI can be found here:  

http://www.acmetoolanddie.com/demo/showtime.html  guide
http://www.acmetoolanddie.com/demo/connectarrow.html  demo

   

The setup below merely assembles all the original source code needed later for the big hack.  

********* Getting the basic Knime application source code ***********  
 
the instructions to setup a knime development environment are here  
    https://github.com/knime/knime-sdk-setup	 
After setting up by cloning the knime-sdk-setup project, and following directions under  
      Getting Started  
You should then have three projects appearing in Eclipse  
      org.apache.xmlbeans, org.knime.example.node, and org.knime.sdk.setup.  
Next clone the knime-core project, as per instructions here   
      Work with Source of KNIME Analytics Platform Extensions  
This will get you knime core and knime base ("Extensions" is just a technical term,  
core and base are the main projects that comprise the knime application)  
under the knime-core directory, you will find and import org.knime.base and org.knime.core eclipse projects  
    
The following additional setup must be followed to use the knime labs code from
jsView to handle the results of a workflow and display them in a browser window. 
jsView converts the Knime table data structue to javascript and is also configurable. 
There is an additional hack to get this to work, but first it needs to be available 
and compiling in the IDE  
  
*************** Getting the jsView from Knime Labs *******************  
  
From Eclipse menu, select  
      Help, Install New Software  
As per instructions on knime page https://www.knime.com/downloads/update  
      copy update url from that page, http://update.knime.org/analytics-platform/3.4/ as of Oct 2017  
and enter url in the input field labeled   

      work with:  
Enter any name you want in pop up window with url    
If you've previously used the url to add software it will appear as a selection already  
    
Under Knime Labs Extensions select 

      KNIME Javascript Views  
      KNIME Webservice Client  
Under Sources select   
      Source for KNIME Javascript Views  
      Source for KNIME Webservice Client  
  
complete process by following prompts in wizard to install  

*********** Hacky Way of Installing jsView source code ***************  

from the eclipse installation, go to dir

   Eclipse.app/Contents/Eclipse/plugins
make a directory somewhere to unpack the js.views jar, for example

   mkdir Documents/jsview
copy the js.views jar from the plugins dir to tmp dir
   cp org.knime.js.views.source_3.4.1.v201709070952.jar Documents/jsview
unjar the jar
   jar xf org.knime.js.views.source_3.4.1.v201709070952.jar
go to wherever you cloned the knime core project
   cd /Applications/KnimeFromGit
here you would find two directories
   knime-core   knime-sdk-setup
go to org/knime dir within org.knime.base project   
   cd knime-core/org.knime.base/src/org/knime
copy the js.views files here, recursively with -r option
   cp -r Documents/jsview/org/knime/js .

there are a few plugin dependencies needed still  
open the MANIFEST.MF file found under the META-INF in the org.knime.base project  
go to the dependencies tab (tabs appear on bottom of file window) and add the following plugins 

  org.knime.js.core
  com.google.guava
  org.knime.ext.svg
  org.apache.commons.lang3
  
Also you may have to move the org.knime.core dependency to the the top (select and use up button)

************ To fix remaining errors from abstract method not implemented ********

An abstract method added in August 2017 is not implemented in several jsView descendant classes as of Oct 23, 2017

go to org.knime.core.node.wizard.WizardNode, comment out the method declaration and insert a default implementation 

Here is an example of code after modification with hack and some documentation of such
     
     /**
     * Property set in the configuration dialog to the node to skip this node in the wizard execution.
     * @param hide true if node is to be skipped, false otherwise
     * @since 3.5
     */
    //public void setHideInWizard(final boolean hide);
    /*public void setHideInWizard(final boolean hideInWizard) {
        m_hideInWizard = hideInWizard;
    }*/
    //public void setHideInWizard(final boolean hide);
    default public void setHideInWizard(final boolean hideInWizard) {
    }
    
*************** Getting js.core source from zip file ******************

the org.knime.js.core directory org.knime.js.core_3.4.1.v201709070952 
that contains a jar of class files can be added from Eclipse menu, select
   Help, Install New Software
as described above in ***Getting the jsView from Knime Labs***

But the source is missing from the sources list and must presently be obtained 
by downloading the zipped file of all the plugins, roughly 3 GB, and as unzip 
retains the zip file, you'll need 6.1 GB
As per instructions on https://www.knime.com/downloads/update, the link appear here
   KNIME Analytics Platform: download KNIME Update Site
The actual url of the link being   
   https://update.knime.org/analytics-platform/UpdateSite_latest34.zip
create a separate directory to hold the zip file and subsequent unzipped dirs and 
files, then find the needed source file with shell command
   find ./|grep js.core
   .//org.knime.update.org/plugins/org.knime.js.core.source_3.4.1.v201709070952.jar
   .//org.knime.update.org/plugins/org.knime.js.core_3.4.1.v201709070952.jar

follow similar procedure (hack) used for jsView. cp source jar to a dir

make a directory somewhere to unpack the js.views jar, for example
   mkdir Documents/jscore
copy the js.core jar from the plugins dir to tmp dir
   cp org.knime.js.core.source_3.4.1.v201709070952.jar Documents/jscore
unjar the jar
   jar xf org.knime.js.core.source_3.4.1.v201709070952.jar
go to wherever you cloned the knime core project
   cd /Applications/KnimeFromGit
here you would find two directories
   knime-core   knime-sdk-setup
go to org/knime dir within org.knime.base project   
   cd knime-core/org.knime.base/src/org/knime
copy the js.views files here, recursively with -r option
   cp -r Documents/jscore/org/knime/js .

again there are a several plugin dependencies needed still
open the MANIFEST.MF file found under the META-INF in the org.knime.base project
go to the dependencies tab (tabs appear on bottom of file window) and add the following plugins
  com.fasterxml.jackson.core.jackson-core
  com.fasterxml.jackson.core.jackson-databind
  org.eclipse.jface
  org.elcipse.ui.workbench
  org.apache.commons.codec
  org.openqa.selenium
  org.knime.ext.phantomjs
  org.knime.time

 
******* extraneous note on identifying missing dependencies**********

Looking to identify missing dependency to find import in 7 jsView files for
   import org.knime.base.data.xml.SvgCell;
Using knime search yielded no result for org.knime.base.data.xml.SvgCell or org.knime.base.data.xml, 
searching for just SvgCell will work, but pasting in google came up with this:
  
   https://www.knime.com/forum/knime-developers/cannot-find-orgknimebasedataxml-package-anywhere

which identified 
   org.knime.ext.svg
as the missing dependency to add, saving some probable trial and error, fortunately this had been posted in the
forum just 13 days prior on Oct 10, 2017

************************************************************************
interesting to see code here too https://bitbucket.org/KNIME/knime-core/overview

All additional code and modifications contributed to existing licenced code are 
released under the General Public License (GPL), Version 3 (including certain 
additional permissions according to Sec. 7 of the GPL, see the file LICENSE.TXT, 
It is located in the same directory as the immediate subdirectories of 
this repository. 

**************************************************************************************  
------------------  How to indent on an .md page? (on a mac)  ----------------------  
https://stackoverflow.com/questions/6046263/how-to-indent-a-few-lines-in-markdown-markup     
	
\&nbsp; (Unicode U+00A0) literal characters can be easily typed on macOS with <kbd>option</kbd>-<kbd>spacebar</kbd>. And code editors with good invisibles support (such as TextMate) will show normal spaces as a faint bullet and non-breaking-spaces as a bolder bullet (but still fainter than the text color). ← I think an editor with good invisibles viz is essential for Markdown, especially because of MD's two-spaces-at-end-of-line=<br/> syntax. – Slipp D. Thompson Jun 30 '16 at 2:58 
