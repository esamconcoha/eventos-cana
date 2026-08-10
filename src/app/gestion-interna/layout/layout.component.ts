import { Component, HostListener } from '@angular/core';

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {
  isCollapsed = false;
  isSidebarOpen = false;
  isMobile = window.innerWidth < 1024;

  @HostListener('window:resize')
  onResize() {
    this.isMobile = window.innerWidth < 1024;
    if (!this.isMobile) this.isSidebarOpen = false;
  }

  toggleSidebar() {
    if (this.isMobile) {
      this.isSidebarOpen = !this.isSidebarOpen;
    } else {
      this.isCollapsed = !this.isCollapsed;
    }
  }

  closeSidebar() {
    if (this.isMobile) this.isSidebarOpen = false;
  }
}
