import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { SolicitudService } from '../../services/solicitud.service';
import { ApelacionService } from '../../services/apelacion.service';
import { AuthService } from '../../services/auth.service';
import { Solicitud } from '../../models/solicitud.model';
import { Apelacion } from '../../models/apelacion.model';

@Component({
  selector: 'app-ciudadano-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './ciudadano-dashboard.component.html'
})
export default class CiudadanoDashboardComponent implements OnInit {
  private solicitudService = inject(SolicitudService);
  private apelacionService = inject(ApelacionService);
  private authService = inject(AuthService);
  private router = inject(Router);

  // 📡 Signals reactivos solicitados en el backlog
  solicitudes = signal<Solicitud[]>([]);
  apelaciones = signal<Apelacion[]>([]);
  cargando = signal<boolean>(true);

  // 🧠 Computed: solicitudes con estado RESPONDIDA/DENEGADA/VENCIDA sin apelación previa
  solicitudesApelables = computed(() => {
    const estadosPermitidos = ['RESPONDIDA', 'DENEGADA', 'VENCIDA'];
    // Obtenemos los IDs de las solicitudes que ya tienen una apelación
    const idsYaApeladas = this.apelaciones().map(a => a.solicitudId);

    return this.solicitudes().filter(sol => 
      sol.estado && 
      estadosPermitidos.includes(sol.estado) && 
      sol.idSolicitud && 
      !idsYaApeladas.includes(sol.idSolicitud)
    );
  });

  // Computed booleano solicitado en el ticket
  tieneApelables = computed(() => this.solicitudesApelables().length > 0);

  ngOnInit(): void {
    this.cargarDatosDashboard();
  }

  cargarDatosDashboard() {
    const sesion = this.authService.obtenerSesion();
    if (!sesion || !sesion.ciudadanoId) {
      this.cargando.set(false);
      return;
    }

    const id = sesion.ciudadanoId;
    let peticionesCompletadas = 0;

    // Cargamos Solicitudes
    this.solicitudService.findByCiudadanoId(id).subscribe({
      next: (data) => {
        this.solicitudes.set(data);
        peticionesCompletadas++;
        if (peticionesCompletadas === 2) this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });

    // Cargamos Apelaciones
    this.apelacionService.findByCiudadanoId(id).subscribe({
      next: (data) => {
        this.apelaciones.set(data);
        peticionesCompletadas++;
        if (peticionesCompletadas === 2) this.cargando.set(false);
      },
      error: () => this.cargando.set(false)
    });
  }

  iniciarApelacion(solicitudId: number) {
    // Redirigimos a la nueva pantalla pasando el ID por parámetro
    this.router.navigate(['/ciudadano/nueva-apelacion'], { queryParams: { solicitudId } });
  }
}