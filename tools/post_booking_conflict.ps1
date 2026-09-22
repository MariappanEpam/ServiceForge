$body = Get-Content -Raw 'C:\Users\Mariappan_Ganesan\Documents\New folder\ServiceForge\ServiceForge-Start\temp_booking_conflict.json'
try {
    $r = Invoke-WebRequest -Uri 'http://localhost:8080/api/jobs' -Method Post -Body $body -ContentType 'application/json' -UseBasicParsing -ErrorAction Stop
    Write-Host 'STATUS' $r.StatusCode.Value__
    Write-Host $r.Content
} catch {
    if ($_.Exception.Response) {
        $resp = $_.Exception.Response
        $reader = New-Object System.IO.StreamReader($resp.GetResponseStream())
        $body = $reader.ReadToEnd()
        Write-Host 'STATUS' $resp.StatusCode.Value__
        Write-Host $body
    } else {
        Write-Host 'ERROR' $_.Exception.Message
    }
}