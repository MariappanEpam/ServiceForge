import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { TechnicianCalendarComponent } from './technician-calendar/technician-calendar.component';
import { PartsComponent } from './parts/parts.component';
import { PartsService } from './services/parts.service';

@NgModule({
  declarations: [
    AppComponent,
    TechnicianCalendarComponent
    ,PartsComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
