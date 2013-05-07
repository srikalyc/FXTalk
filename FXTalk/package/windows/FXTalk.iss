;This file will be executed next to the application bundle image
;I.e. current directory will contain folder FXTalk with application files
[Setup]
AppId={{FXTalkApp}}
AppName=FXTalk
AppVersion=1.0
AppVerName=FXTalk 1.0
AppPublisher=srikalyc
AppComments=FXTalk
AppCopyright=
;AppPublisherURL=http://java.com/
;AppSupportURL=http://java.com/
;AppUpdatesURL=http://java.com/
DefaultDirName={localappdata}\FXTalk
DisableStartupPrompt=Yes
DisableDirPage=Yes
DisableProgramGroupPage=Yes
DisableReadyPage=Yes
DisableFinishedPage=Yes
DisableWelcomePage=Yes
DefaultGroupName=srikalyc
;Optional License
LicenseFile=
;WinXP or above
MinVersion=0,5.1 
OutputBaseFilename=FXTalk-1.0
Compression=lzma
SolidCompression=yes
PrivilegesRequired=lowest
SetupIconFile=FXTalk\FXTalk.ico
UninstallDisplayIcon={app}\FXTalk.ico
UninstallDisplayName=FXTalk
WizardImageStretch=No
WizardSmallImageFile=FXTalk-setup-icon.bmp   

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Files]
Source: "FXTalk\FXTalk.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "FXTalk\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\FXTalk"; Filename: "{app}\FXTalk.exe"; IconFilename: "{app}\FXTalk.ico"; Check: returnTrue()
Name: "{commondesktop}\FXTalk"; Filename: "{app}\FXTalk.exe";  IconFilename: "{app}\FXTalk.ico"; Check: returnFalse()

[Run]
Filename: "{app}\FXTalk.exe"; Description: "{cm:LaunchProgram,FXTalk}"; Flags: nowait postinstall skipifsilent

[Code]
function returnTrue(): Boolean;
begin
  Result := True;
end;

function returnFalse(): Boolean;
begin
  Result := False;
end;

function InitializeSetup(): Boolean;
begin
// Possible future improvements:
//   if version less or same => just launch app
//   if upgrade => check if same app is running and wait for it to exit
//   Add pack200/unpack200 support? 
  Result := True;
end;  
