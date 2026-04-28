import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';

type DecisionType = 'admitido' | 'improcedente' | null;
type TabType = 'subsanacion' | 'adjuntos';

@Component({
  selector: 'app-ttaip-segunda-calificacion',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './ttaip-segunda-calificacion.component.html'
})
export class TtaipSegundaCalificacionComponent {
  expediente: string = '00237-2025-JUS/TTAIP';
  activeTab: TabType = 'subsanacion';
  decision: DecisionType = null;
  fundamentos: string = '';
  showPreview: boolean = false;

  constructor(private route: ActivatedRoute) {
    this.expediente = this.route.snapshot.paramMap.get('expediente') || '00237-2025-JUS/TTAIP';
  }

  handleEmitir() {
    if (!this.decision || !this.fundamentos.trim()) {
      alert('Complete todos los campos obligatorios.');
      return;
    }

    if (this.decision === 'admitido') {
      alert('2da Calificación emitida: ADMITIDO A TRÁMITE');
    } else {
      alert('2da Calificación emitida: IMPROCEDENTE');
    }
  }
}
