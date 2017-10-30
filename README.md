# browserUI
All product and company names are trademarks™ or registered® trademarks of their respective holders. 
Use of the name Knime does not imply any affiliation with or endorsement by the company.   

The instructions given below comprise the needed steps to set up Knime source code in Eclipse.  The source code includes  that made availble from installing the Knime SDK ( the entire SDK from https://github.com/knime/knime-sdk-setup ), the basic source code of the Knime application ( base and core projects from https://github.com/knime/knime-core ) and additional Knime source code for javascript related classes (java classes and some javascript libs from js.view and js.core jars). You will find below a summarization of or reference to the instructions from the Knime website, to clone and import code into Eclipse 4.6.1, for the purpose of adding custom nodes, and  examination or modification of the code. Additional instructions are then provided to access the source code of the javascript related classes as well.  The Knime version is 3.4.2, and please note that Knime warns against using Eclipse 4.6.2  due to problems compiling with that version. These files and setup were assembled and current as of mid October 2017.  

The purpose of this project is to expose a version of Knime that demonstrates a browser workflow UI. The browser workflow UI is a UI for workflow creation and execution, accessible through any web browser. The version being featured with links from this repo has been engineered to leverage existing code. The goal has been to create a working prototype with as little code modification as possible. The least modification means expanding the code later to encompass full functionality can presumably be done in the most efficient and scalable manner.  

The main components of the Knime application and this setup, aside from the Knime SDK, are org.knime.base and org.knime.core. Additionally, org.knime.js.core and org.knime.js.views are incorporated into the base project as a convenient shortcut (or hack) instead of being set up as thier own projects. They are needed to  display the output of workflows in the browser. js.views is a component of Knime Labs, meaning it is  an additional package not yet included in the standard distribution.  

The setup described below installs the Knime source code needing modification to create a browser UI. 

The additional code and modification of Knime code to create the browser UI is presently unavailable from this public repo, awaiting completion of an extensive refactoring task.  That new and modified code (not created by Knime) was produced in a very experimental manner, likewise the javascript (not created by Knime) used in the POC (proof of concept) is also currently being refactored, to be added here when that task is complete.

A simple working version of the browser UI can already be found here (a big hack):  

http://www.acmetoolanddie.com/demo/showtime.html  guide    
http://www.acmetoolanddie.com/demo/connectarrow.html  demo    

Directions below merely assemble all the original Knime source code needed later to create or modify the big hack.  

********* Getting the basic Knime application source code ***********  
 
the instructions to setup a knime development environment are here      
      https://github.com/knime/knime-sdk-setup    
After setting up by cloning the knime-sdk-setup project, and following directions under  
      Getting Started  
You should then have three projects appearing in Eclipse  
      org.apache.xmlbeans, org.knime.example.node, and org.knime.sdk.setup.
      
At this point, one can either clone and import the org.knime.base and org.knime.core Eclipse projects from this repo    
git clone https://github.com/oct15demo/browserUI.git
   
   or
   
Follw the directions below to retrieve more code form the Knime repo. Those directions will leave you with the necessary Knime code in Eclipse, before any additions and modifications have been made that are necessary for the browser UI.    

Note: As of this writing, Oct 30, as stated earlier, this repo http://github.com/oct15demo/browserUI/ also doesn't yet include the additions and modifications for the browser UI, as they are still in the process of being refactored. However, the unaltered required javascript sources files are much easier to set up from this browserUI repo because they're already added to the org.knime.base project, no extra steps are required. But to continue to check out everything fresh and unaltered from Knime:
      
Clone the knime-core project, as per instructions on that setup page under heading   
      Work with Source of KNIME Analytics Platform Extensions  
"Extensions" is just a technical term, org.knime.core and org.knime.base are the main projects that comprise the knime application. You will find the org.knime.base and org.knime.core eclipse projects under the knime-core directory after cloning that repository.  
    https://github.com/knime/knime-core
    
The following additional setup steps must be followed to check out the js.view code from Knime Labs, and to import the js.core source code. The js.core and js.view sources are needed to process results of browser UI workflows if one is to display those results in a browser window. jsView converts the Knime table data structue to javascript and is also configurable.    

Checking out the code below directly from Knime does not include the additions and modifications necessary for the browser UI. It is given as a reference to anyone wishing to understand how the environment is originally configured, and the process might be necessary to merge future versions from Knime with this repo. More likely, a slicker way to accomplish keeping the code up to date would be determined and adopted.    
  
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
as described above in   
\*\*\*\*Getting the jsView from Knime Labs\*\*\*\*

However, the source is missing from the sources list and must presently be obtained 
by downloading the zipped file of all the plugins, roughly 3 GB, and as the unzip 
utility retains the zip file, be aware you'll actually need 6.1 GB of free space.        
As per instructions on https://www.knime.com/downloads/update, the link appears as    
    KNIME Analytics Platform: download KNIME Update Site
The actual url of the link being       
    https://update.knime.org/analytics-platform/UpdateSite_latest34.zip
create a separate directory to hold the zip file and subsequent unzipped dirs and files, then find the needed source file with shell command    
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
    knime-core   knime-sdk-setup    
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
Using knime search yielded no result for org.knime.base.data.xml.SvgCell or org.knime.base.data.xml, searching for just SvgCell will work, but pasting in google came up with this:    
  
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
