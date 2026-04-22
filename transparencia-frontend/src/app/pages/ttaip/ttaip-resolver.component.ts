import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TtaipService } from '../../services/ttaip.service';

@Component({
  selector: 'app-ttaip-resolver',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './ttaip-resolver.component.html'
})
export class TtaipResolverComponent implements OnInit {
  expediente: string = '';
  tabActivo: string = 'apelacion';

  // Variables del formulario (las que usa tu HTML con ngModel)
  causalAplicada: string = '';
  decision: string = '';
  iniciarProcesoDisciplinario: boolean = false;
  fundamentos: string = '';

  // Datos mock para la vista (Trazabilidad)
  apelacionData = {
    ciudadano: 'Juan Carlos Pérez Mendoza',
    causalDenegatoria: 'Información Confidencial',
    articuloLegal: 'Art. 17° Ley N° 27806',
    detalleCausal: 'La información relacionada con proyectos en etapa de planificación no es pública.',
    argumentoCiudadano: 'El presupuesto ya fue aprobado mediante Ley de Presupuesto, por lo que no es planificación.'
  };

  adjuntos = [
    { nombre: 'Recurso_Fundamentado.pdf', tamano: '1.2 MB' },
    { nombre: 'Respuesta_Denegatoria.pdf', tamano: '450 KB' }
  ];

  // Las 7 opciones exactas que pide el Backlog
  opcionesFallo = [
    { id: 'FUNDADO', tipo: 'check', titulo: 'FUNDADO', desc: 'Revoca la denegatoria. La entidad debe entregar la información.' },
    { id: 'FUNDADO_PARTE', tipo: 'check', titulo: 'FUNDADO EN PARTE', desc: 'Revoca parcialmente. Se entrega solo una parte de lo solicitado.' },
    { id: 'INFUNDADO', tipo: 'x', titulo: 'INFUNDADO', desc: 'Confirma la denegatoria de la entidad. No procede la entrega.' },
    { id: 'INFUNDADO_PARTE', tipo: 'x', titulo: 'INFUNDADO EN PARTE', desc: 'Confirma parcialmente la denegatoria de la entidad.' },
    { id: 'IMPROCEDENTE', tipo: 'alert', titulo: 'IMPROCEDENTE', desc: 'El recurso no cumple con los requisitos de fondo para ser evaluado.' },
    { id: 'SUSTRACCION', tipo: 'alert', titulo: 'CONCLUSIÓN: Sustracción de la Materia', desc: 'La entidad entregó la información durante el proceso de apelación.' },
    { id: 'DESISTIMIENTO', tipo: 'alert', titulo: 'CONCLUSIÓN: Desistimiento', desc: 'El ciudadano se retiró voluntariamente del proceso.' }
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private ttaipService: TtaipService // Inyecta el servicio que arreglamos
  ) {}

  ngOnInit(): void {
    this.expediente = this.route.snapshot.paramMap.get('expediente') || '00234-2025-JUS/TTAIP';
  }

  onCausalChange() {
    // Si la causal está mal aplicada, sugerimos Fundado. Si está bien, sugerimos Infundado.
    if (this.causalAplicada === 'REVOCAR') {
      this.decision = 'FUNDADO';
    } else if (this.causalAplicada === 'CONFIRMAR') {
      this.decision = 'INFUNDADO';
      this.iniciarProcesoDisciplinario = false;
    }
  }

  handleEmitResolution() {
    if (!this.decision || this.fundamentos.length < 15) return;

    // Arma el objeto con los datos que espera el backend
    const data = {
      decision: this.decision,
      fundamentos: this.fundamentos,
      iniciarProcesoDisciplinario: this.iniciarProcesoDisciplinario
    };

    // servicio simulado de la tarea anterior
    this.ttaipService.declararFundado(this.expediente, data).subscribe({
      next: () => {
        alert('¡Resolución Final emitida con éxito!');
        this.router.navigate(['/ttaip']); // Regresa al dashboard tras guardar
      },
      error: (err) => {
        console.error('Error al emitir resolución', err);
        alert('Ocurrió un error al intentar guardar la resolución.');
      }
    });
  }
  descargarResolucionFinal() {
    // Simular la creación y descarga de un PDF
    const contenido = `Resolución Final del expediente: ${this.expediente}\nDecisión: ${this.decision}`;
    const blob = new Blob([contenido], { type: 'application/pdf' });
    const url = window.URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = `Resolucion_${this.expediente}.pdf`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    window.URL.revokeObjectURL(url);
  }
}
