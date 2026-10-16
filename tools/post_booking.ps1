$body = Get-Content -Raw 'C:\Users\Mariappan_Ganesan\Documents\New folder\ServiceForge\ServiceForge-Start\temp_booking.json'
try {
    $resp = Invoke-RestMethod -Uri 'http://localhost:8080/api/jobs' -Method Post -Body $body -ContentType 'application/json' -ErrorAction Stop
    Write-Host 'SUCCESS'
    $resp | ConvertTo-Json -Depth 5
} catch {
    if ($_.Exception.Response) {
        $r = $_.Exception.Response
        $status = $r.StatusCode.Value__
        $reader = New-Object System.IO.StreamReader($r.GetResponseStream())
        $body = $reader.ReadToEnd()
        Write-Host 'STATUS' $status
        Write-Host $body
    } else {
        Write-Host 'ERROR' $_.Exception.Message
    }
}