; setup.nsi
;
; This script is based on example1.nsi, but it remember the directory, 
; has uninstall support and (optionally) installs start menu shortcuts.
;
; It will install mazio.nsi into a directory that the user selects,

!include WordFunc.nsh
!insertmacro VersionCompare
 
!include LogicLib.nsh

!include MUI.nsh
;--------------------------------

; The name of the installer
Name "Atlassian Visual Studio Connector"

LicenseData LICENSE

; The file to write
OutFile "bin\release\atlassian-vs-connector-setup.exe"

; The default installation directory
InstallDir "$PROGRAMFILES\Atlassian\Visual Studio Connector"

; Registry key to check for directory (so if you install again, it will 
; overwrite the old one automatically)
InstallDirRegKey HKLM "Software\Atlassian\PaZu" "Install_Dir"

; Request application privileges for Windows Vista
RequestExecutionLevel admin

;--------------------------------

; Pages

!define MUI_ICON "icons\ide_plugin_32.ico"
!define MUI_UNICON "icons\ide_plugin_32.ico"
!define MUI_WELCOMEFINISHPAGE_BITMAP "icons\atlassian-installer.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "icons\atlassian-installer.bmp"
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE LICENSE
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES
 
!define MUI_FINISHPAGE_NOAUTOCLOSE
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

!insertmacro MUI_LANGUAGE "English"

;--------------------------------

; The stuff to install
Section "PaZu (required)"

  SectionIn RO
  
  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File "bin\release\pazunet.dll"  
  File "bin\release\Aga.Controls.dll"
  File "pazunet.AddIn"
  
  CreateDirectory $APPDATA\Microsoft\MSEnvShared\AddIns\pazunet
  CopyFiles $INSTDIR\pazunet.dll $APPDATA\Microsoft\MSEnvShared\AddIns\pazunet
  IfErrors 0 +2
	Abort "Unable to copy pazunet.dll file to the Visual Studio Add-in directory"
  CopyFiles $INSTDIR\Aga.Controls.dll $APPDATA\Microsoft\MSEnvShared\AddIns\pazunet
  IfErrors 0 +2
	Abort "Unable to copy Aga.Controls.dll file to the Visual Studio Add-in directory"
  CopyFiles $INSTDIR\pazunet.AddIn $APPDATA\Microsoft\MSEnvShared\AddIns
  IfErrors 0 +2
	Abort "Unable to copy pazunet.AddIn file to the Visual Studio Add-in directory"

  ; Write the installation path into the registry
  WriteRegStr HKLM SOFTWARE\Atlassian\PaZu "Install_Dir" "$INSTDIR"
  
  ; Write the uninstall keys for Windows
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PaZu" "DisplayName" "Atlassian Visual Studio Connector"
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PaZu" "UninstallString" '"$INSTDIR\uninstall.exe"'
  WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PaZu" "DisplayIcon" '"$INSTDIR\uninstall.exe"'
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PaZu" "NoModify" 1
  WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PaZu" "NoRepair" 1
  WriteUninstaller "$INSTDIR\uninstall.exe"
  
SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"
  
  ; Remove registry keys
  DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\PaZu"
  DeleteRegKey HKLM SOFTWARE\Atlassian\PaZu

  ; Remove files and uninstaller
  Delete $INSTDIR\pazunet.dll
  Delete $INSTDIR\Aga.Controls.dll
  Delete $INSTDIR\pazunet.AddIn
  Delete $APPDATA\Microsoft\MSEnvShared\AddIns\pazunet\pazunet.dll
  Delete $APPDATA\Microsoft\MSEnvShared\AddIns\pazunet\Aga.Controls.dll
  Delete $APPDATA\Microsoft\MSEnvShared\AddIns\pazunet.AddIn
  Delete $INSTDIR\uninstall.exe

  ; Remove directories used
  RMDir "$APPDATA\Microsoft\MSEnvShared\AddIns\pazunet"
  RMDir "$INSTDIR"

SectionEnd