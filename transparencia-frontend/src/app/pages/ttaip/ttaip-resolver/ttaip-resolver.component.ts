import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-ttaip-resolver',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './ttaip-resolver.component.html',
  styleUrl: './ttaip-resolver.component.css' // Asegúrate de tener este archivo o quitar esta línea si no usas CSS específico
})
export class TtaipResolverComponent implements OnInit {
  expediente: string = '';
  tabActivo: 'apelacion' | 'adjuntos' = 'apelacion';

  // Datos simulados del expediente (Dinámicos)
  apelacionData = {
    ciudadano: 'Juan Carlos Pérez Mendoza',
    correo: 'juan.perez&#64;example.com',
    expedienteSaip: 'SAIP-2025-00512',
    asuntoSaip: 'Información sobre presupuesto de infraestructura educativa 2025',
    causalDenegatoria: 'Información Confidencial', // <--- Esta es la causal dinámica
    articuloLegal: 'Art. 17° de la Ley N° 27806',
    detalleCausal: 'La información relacionada con proyectos en etapa de planificación que no han sido oficialmente aprobados...',
    argumentoCiudadano: 'El ciudadano argumenta que la información solicitada corresponde a presupuesto ya ejecutado y aprobado en el ejercicio fiscal 2024...'
  };

  // Lista de Adjuntos poblada
  adjuntos = [
    { nombre: 'Recurso_Apelacion_Firmado.pdf', tamano: '2.1 MB' },
    { nombre: 'Respuesta_Entidad_Denegatoria.pdf', tamano: '1.5 MB' },
    { nombre: 'Anexo_Pruebas_Ciudadano.pdf', tamano: '4.8 MB' },
    { nombre: 'Hoja_Ruta_Expediente.pdf', tamano: '0.9 MB' }
  ];

  // Estado del Formulario
  causalAplicada: 'CONFIRMAR' | 'REVOCAR' | null = null;
  decision: string = '';
  fundamentos: string = '';
  iniciarProcesoDisciplinario: boolean = false;

  // Estructura de Opciones de Fallo con íconos y colores de Figma
  opcionesFallo = [
    { id: 'FUNDADO', titulo: 'FUNDADO', desc: 'Se ampara el recurso - Se ordena entregar la informacion', tipo: 'check' },
    { id: 'FUNDADO_PARTE', titulo: 'FUNDADO EN PARTE', desc: 'Se ampara parcialmente - Entrega parcial de informacion', tipo: 'check' },
    { id: 'INFUNDADO', titulo: 'INFUNDADO', desc: 'Se desestima el recurso - Confirma conducta de la entidad', tipo: 'x' },
    { id: 'INFUNDADO_PARTE', titulo: 'INFUNDADO EN PARTE', desc: 'Se desestima parcialmente - Confirma parte de la conducta', tipo: 'x' },
    { id: 'IMPROCEDENTE', titulo: 'IMPROCEDENTE', desc: 'No cumple requisitos formales o de fondo', tipo: 'alert' },
    { id: 'SUSTRACCION', titulo: 'CONCLUSIÓN POR SUSTRACCIÓN DE LA MATERIA', desc: 'La entidad entregó la información durante el trámite', tipo: 'alert' },
    { id: 'DESISTIMIENTO', titulo: 'CONCLUSIÓN POR DESISTIMIENTO', desc: 'El ciudadano desiste voluntariamente del recurso', tipo: 'alert' }
  ];

  constructor(private route: ActivatedRoute, private router: Router) {}

  ngOnInit(): void {
    this.expediente = this.route.snapshot.paramMap.get('expediente') || '00150-2025-JUS/TTAIP';
  }

  // Lógica Automática de Figma
  onCausalChange() {
    if (this.causalAplicada === 'REVOCAR') {
      this.decision = 'FUNDADO';
    } else if (this.causalAplicada === 'CONFIRMAR') {
      this.decision = 'INFUNDADO';
    }
  }

  handleEmitResolution() {
    alert('Resolución Final emitida exitosamente (Simulación).');
    this.router.navigate(['/ttaip']);
  }
}
