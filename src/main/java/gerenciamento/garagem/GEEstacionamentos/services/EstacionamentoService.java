package gerenciamento.garagem.GEEstacionamentos.services;

import gerenciamento.garagem.GEEstacionamentos.models.domain.*;
import gerenciamento.garagem.GEEstacionamentos.models.dto.CriaEstacionamentoDTO;
import gerenciamento.garagem.GEEstacionamentos.models.dto.NovoFuncionarioDTO;
import gerenciamento.garagem.GEEstacionamentos.resources.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.Optional;

@Service
public class EstacionamentoService {
    @Autowired
    private EstacionamentoInterface estacionamentoInterface;
    @Autowired
    private CidadeInterface cidadeInterface;
    @Autowired
    private EnderecoEstacionamentoInterface enderecoEstacionamentoInterface;
    @Autowired
    private EstadoInterface estadoInterface;
    @Autowired
    private FuncionarioInterface fcInterface;

    public boolean save(CriaEstacionamentoDTO estacionamentoDTO) throws Exception {
        Estado estado = dtoToModelEstado(estacionamentoDTO);
        if (estado.getId() == 0) {
            estado = saveEstado(estado);
        }
        Cidade cidade = dtoToModelCidade(estacionamentoDTO);
        if (cidade.getId() == 0) {
            cidade.setEstado(estado);
            cidade = saveCidade(cidade);
        }
        EnderecoEstacionamento endereco = dtoToModelEndereco(estacionamentoDTO, cidade);
        if (endereco.getId() == 0) {
            endereco = saveEndereco(endereco);
        }
        if (estacionamentoInterface.existsByEndereco(endereco)) {
            throw new Exception("Já existe uma Garagem anexada a este endereço!");
        } else {
            Estacionamento estacionamento = new Estacionamento();
            estacionamento.setEndereco(endereco);
            estacionamento.setNome(estacionamentoDTO.getEstacionamentoNome());
            estacionamento.setCodigoestacionamento(estacionamentoDTO.getCodigoestacionamento());
            estacionamentoInterface.save(estacionamento);
            return true;
        }
    }

    public EnderecoEstacionamento saveEndereco(EnderecoEstacionamento endereco) {
        return enderecoEstacionamentoInterface.save(endereco);
    }

    Cidade saveCidade(Cidade cidade) {
        return cidadeInterface.save(cidade);
    }

    Estado saveEstado(Estado estado) {
        return estadoInterface.save(estado);
    }

    public EnderecoEstacionamento dtoToModelEndereco(CriaEstacionamentoDTO estacionamentoDTO, Cidade cidade) {
        if (enderecoEstacionamentoInterface.existsByRuaAndLogradouroAndNumAndCidade(estacionamentoDTO.getEnderecoRua(),
                estacionamentoDTO.getEnderecoLogradouro(), estacionamentoDTO.getEnderecoNum(), cidade)) {
            return enderecoEstacionamentoInterface.findByRuaAndLogradouroAndNumAndCidade(estacionamentoDTO.getEnderecoRua(),
                    estacionamentoDTO.getEnderecoLogradouro(), estacionamentoDTO.getEnderecoNum(), cidade);
        } else {
            EnderecoEstacionamento estacionamento = new EnderecoEstacionamento();
            estacionamento.setRua(estacionamentoDTO.getEnderecoRua());
            estacionamento.setNum(estacionamentoDTO.getEnderecoNum());
            estacionamento.setLogradouro(estacionamentoDTO.getEnderecoLogradouro());
            estacionamento.setCidade(cidade);
            return estacionamento;
        }
    }

    public Cidade dtoToModelCidade(CriaEstacionamentoDTO estacionamentoDTO) {
        if (cidadeInterface.existsByNomeAndEstado(estacionamentoDTO.getCidadeNome(), estadoInterface.findByNome(estacionamentoDTO.getEstadoNome()))) {
            return cidadeInterface.findByNomeAndEstado(estacionamentoDTO.getCidadeNome(), estadoInterface.findByNome(estacionamentoDTO.getEstadoNome()));
        } else {
            Cidade cidade = new Cidade();
            cidade.setNome(estacionamentoDTO.getCidadeNome());
            return cidade;
        }
    }

    public Estado dtoToModelEstado(CriaEstacionamentoDTO estacionamentoDTO) {
        if (estadoInterface.existsByNome(estacionamentoDTO.getEstadoNome())) {
            return estadoInterface.findByNome(estacionamentoDTO.getEstadoNome());
        } else {
            Estado estado = new Estado();
            estado.setNome(estacionamentoDTO.getEstadoNome());
            estado.setUF(estacionamentoDTO.getEstadoUF());
            return estado;
        }
    }

    public boolean cadastraFuncionario(NovoFuncionarioDTO dto){
        Funcionario funcio = dtoToModelFuncionario(dto);
        funcio = fcInterface.save(funcio);
        return fcInterface.existsById(funcio.getId());
    }

    private Funcionario dtoToModelFuncionario(NovoFuncionarioDTO dto) {
        Optional<Funcionario> optF = fcInterface.findByCpf(dto.getCpf());
        if(optF.isPresent()){
            return optF.get();
        } else{
            Funcionario func = new Funcionario();
            func.setNome(dto.getNome());
            func.setDatanasc(dto.getDtnasc());
            func.setCpf(dto.getCpf());
            func.setEmail(dto.getEmail());
            func.setSenha(dto.getSenha());
            func.setEstacionamento(estacionamentoInterface.findByCodigoestacionamento(dto.getCodigoestacionamento()).get());
            return func;
        }
    }
}
