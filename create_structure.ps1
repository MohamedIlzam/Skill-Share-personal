$base = "C:\Users\lap.lk\IdeaProjects\Skill-Share\Skill-Share\src\main\java\com\skillshare\skillshare"

$files = @(
    @("controller\AuthController.java", "com.skillshare.skillshare.controller", "class", "AuthController"),
    @("controller\UserController.java", "com.skillshare.skillshare.controller", "class", "UserController"),
    @("service\auth\AuthService.java", "com.skillshare.skillshare.service.auth", "interface", "AuthService"),
    @("service\auth\AuthServiceImpl.java", "com.skillshare.skillshare.service.auth", "class", "AuthServiceImpl"),
    @("service\user\UserService.java", "com.skillshare.skillshare.service.user", "interface", "UserService"),
    @("service\user\UserServiceImpl.java", "com.skillshare.skillshare.service.user", "class", "UserServiceImpl"),
    @("repository\UserRepository.java", "com.skillshare.skillshare.repository", "interface", "UserRepository"),
    @("model\user\User.java", "com.skillshare.skillshare.model.user", "class", "User"),
    @("model\user\Role.java", "com.skillshare.skillshare.model.user", "enum", "Role"),
    @("dto\auth\RegisterRequest.java", "com.skillshare.skillshare.dto.auth", "class", "RegisterRequest"),
    @("dto\auth\RegisterResponse.java", "com.skillshare.skillshare.dto.auth", "class", "RegisterResponse"),
    @("dto\user\UserResponse.java", "com.skillshare.skillshare.dto.user", "class", "UserResponse"),
    @("mapper\UserMapper.java", "com.skillshare.skillshare.mapper", "interface", "UserMapper"),
    @("config\SecurityBeans.java", "com.skillshare.skillshare.config", "class", "SecurityBeans"),
    @("config\SecurityConfig.java", "com.skillshare.skillshare.config", "class", "SecurityConfig"),
    @("exception\ApiExceptionHandler.java", "com.skillshare.skillshare.exception", "class", "ApiExceptionHandler"),
    @("exception\NotFoundException.java", "com.skillshare.skillshare.exception", "class", "NotFoundException"),
    @("exception\ResourceConflictException.java", "com.skillshare.skillshare.exception", "class", "ResourceConflictException"),
    @("runner\UserCrudSmokeTestRunner.java", "com.skillshare.skillshare.runner", "class", "UserCrudSmokeTestRunner")
)

foreach ($f in $files) {
    $path = $f[0]
    $pkg = $f[1]
    $type = $f[2]
    $name = $f[3]
    
    $fullPath = "$base\$path"
    $dir = Split-Path $fullPath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    
    $content = "package $pkg;`r`n`r`npublic $type $name {`r`n}`r`n"
    [System.IO.File]::WriteAllText($fullPath, $content)
}

Write-Host "Structure created."
