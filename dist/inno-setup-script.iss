; Script generated by the Inno Setup Script Wizard.
; SEE THE DOCUMENTATION FOR DETAILS ON CREATING INNO SETUP SCRIPT FILES!

#define MyAppName "JSatTrak Eclipse"
#define MyAppVersion "4.2.3"
#define MyAppPublisher "Shawn Gano et al"
#define MyAppURL "https://github.com/Scicrop/JSatTrak"
#define MyAppExeName "jsattrak-eclipse.exe"

[Setup]
; NOTE: The value of AppId uniquely identifies this application. Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{34E62000-8DA0-4BD8-966D-9A60111EF6AA}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={autopf}\{#MyAppName}
DisableProgramGroupPage=yes
LicenseFile=C:\git\JSatTrak\APACHE-LICENSE-2.0.txt
; Uncomment the following line to run in non administrative install mode (install for current user only.)
;PrivilegesRequired=lowest
OutputDir=C:\git\JSatTrak\dist\bin
OutputBaseFilename=jsattrak-eclipse-win64-setup
SetupIconFile=C:\git\JSatTrak\dist\JSatTrakLogo_128.ico
Compression=lzma
SolidCompression=yes
WizardStyle=modern

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked

[Files]
Source: "C:\git\JSatTrak\dist\start-jsattrak.bat"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\git\JSatTrak\dist\bin\jsattrak.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\git\JSatTrak\dist\bin\jsattrak-eclipse.exe"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\git\JSatTrak\data\*"; DestDir: "{app}\data"; Flags: ignoreversion recursesubdirs createallsubdirs
;Source: "C:\git\JSatTrak\temp_images\"; DestDir: "{app}\temp_images"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "C:\git\JSatTrak\plugins\*"; DestDir: "{app}\plugins"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "C:\git\JSatTrak\dist\win64\*"; DestDir: "{app}\win64"; Flags: ignoreversion recursesubdirs createallsubdirs
Source: "C:\git\JSatTrak\dist\win32\*"; DestDir: "{app}\win32"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Dirs]
Name: "{app}\temp_images"

[Icons]
Name: "{autoprograms}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{autodesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: nowait postinstall skipifsilent

[Code]
function CutJavaVersionPart(var V: string): Integer;
var
  S: string;
  P: Integer;
begin
  if Length(V) = 0 then
  begin
    Result := 0;
  end
    else
  begin
    P := Pos('.', V);
    if P = 0 then P := Pos('_', V);

    if P > 0 then
    begin
      S := Copy(V, 1, P - 1);
      Delete(V, 1, P);
    end
      else
    begin
      S := V;
      V := '';
    end;
    Result := StrToIntDef(S, 0);
  end;
end;

function MaxJavaVersion(V1, V2: string): string;
var
  Part1, Part2: Integer;
  Buf1, Buf2: string;
begin
  Buf1 := V1;
  Buf2 := V2;
  Result := '';
  while (Result = '') and
        ((Buf1 <> '') or (Buf2 <> '')) do
  begin
    Part1 := CutJavaVersionPart(Buf1);
    Part2 := CutJavaVersionPart(Buf2);
    if Part1 > Part2 then Result := V1
      else
    if Part2 > Part1 then Result := V2;
  end;
end;

function GetJavaVersionFromSubKey(RootKey: Integer; SubKeyName: string): string;
var
  Versions: TArrayOfString;
  I: Integer;
begin
  if RegGetSubkeyNames(RootKey, SubKeyName, Versions) then
  begin
    for I := 0 to GetArrayLength(Versions) - 1 do
    begin
      Result := MaxJavaVersion(Result, Versions[I]);
    end;
  end;
end;

function GetJavaVersionFromRootKey(RootKey: Integer): string;
begin
  Result := 
    MaxJavaVersion(
      GetJavaVersionFromSubKey(RootKey, 'SOFTWARE\JavaSoft\Java Runtime Environment'),
      GetJavaVersionFromSubKey(RootKey, 'SOFTWARE\JavaSoft\Java Development Kit'));
end;

function GetJavaVersion: string;
begin
  Result := GetJavaVersionFromRootKey(HKLM);
  if IsWin64 then
  begin
    Result := MaxJavaVersion(Result, GetJavaVersionFromRootKey(HKLM64));
  end;
end;

function HasJava1Dot7OrNewer: Boolean;
begin
  Result := (MaxJavaVersion('1.6.9', GetJavaVersion) <> '1.6.9');
end;

function InitializeSetup(): Boolean;
var
  ErrorCode: Integer;
begin
  Result := HasJava1Dot7OrNewer;
  if not Result then
  begin
    Result := MsgBox('Java not found, do you want to continue, anyway?', mbConfirmation, MB_YESNO) = idYes;
    if Result then
    begin
      ShellExec(
        'open', 'https://www.java.com/getjava/', '', '', SW_SHOWNORMAL, ewNoWait, ErrorCode);
    end;
  end;
end;
