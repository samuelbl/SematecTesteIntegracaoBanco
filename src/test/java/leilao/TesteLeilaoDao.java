package leilao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.sematec.leilao.dao.CriadorDeSessao;
import br.com.sematec.leilao.dao.LeilaoDao;
import br.com.sematec.leilao.dao.UsuarioDao;
import br.com.sematec.leilao.dominio.Leilao;
import br.com.sematec.leilao.dominio.Usuario;

public class TesteLeilaoDao {
	// deveEncontrarPeloNomeEEmailMockado()
	private Session session;
	private LeilaoDao leilaoDao;
	private UsuarioDao usuarioDao;
	private Usuario teste;
	private Leilao leilaoGeladeira;
	private Leilao leilaoXbox;

	@Before
	public void antes(){
		session = new CriadorDeSessao().getSession();
		leilaoDao = new LeilaoDao(session);
		usuarioDao = new UsuarioDao(session);
		teste = new Usuario("teste","teste@teste.com.br");
		leilaoGeladeira = new Leilao("geladeira", 1500.0, teste, true);
		leilaoXbox = new Leilao("XBox", 700.0, teste, false);
		session.beginTransaction();
	}
	
	@After
	public void depois(){
		session.getTransaction().rollback();
		session.close();
		
	}
	

	
	
	@Test
	public void deveConterLeiloesNaoEncerrados() {
		
		//ciramos um usuario
		// criamos os dois leiloes
		Leilao ativo = leilaoGeladeira;
		Leilao encerrado = leilaoXbox;
		encerrado.encerra();
		
		//persistimos no banco
		usuarioDao.salvar(teste);
		leilaoDao.salvar(ativo);
		leilaoDao.salvar(encerrado);
		
		//invocamos a ação que queremos testar pedimos o total para o DAO
		long total = leilaoDao.total();
		assertEquals(1l, total);
		
		leilaoDao.deleta(ativo);
		leilaoDao.deleta(encerrado);
		usuarioDao.deletar(teste);
	}
	
	
	
	

	@Test
	public void deveTrazerSomenteLeiloesAntigos() {
		
		Leilao antigo = leilaoGeladeira;
		Leilao recente = leilaoXbox;
		recente.setDataAbertura(Calendar.getInstance());
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.add(Calendar.DAY_OF_MONTH, -10);
		antigo.setDataAbertura(dataAntiga);
		usuarioDao.salvar(teste);
		leilaoDao.salvar(antigo);
		leilaoDao.salvar(recente);
				
		List<Leilao> antigos = leilaoDao.antigos();
		assertEquals(1, antigos.size());
		assertEquals("geladeira", antigos.get(0).getNome());
	}
	
	@Test
	public void deveRetornarLeiloesDeProdutosNovos(){
		Leilao produtoNovo =  leilaoXbox;
		Leilao produtoUsado = leilaoGeladeira;
		
		usuarioDao.salvar(teste);
		leilaoDao.salvar(produtoNovo);
		leilaoDao.salvar(produtoUsado);
		
		List<Leilao> novos = leilaoDao.novos();
		assertEquals(1, novos.size());
		assertEquals("XBox", novos.get(0).getNome());
	}
	
	@Test
	public void deveRetornarZeroSeNaoHaLeiloesNovos(){
		Leilao encerrado =  leilaoXbox;
		Leilao tambemEncerrado = leilaoGeladeira;
		
		encerrado.encerra();
		tambemEncerrado.encerra();
		usuarioDao.salvar(teste);
		leilaoDao.salvar(encerrado);
		leilaoDao.salvar(tambemEncerrado);
		
		long total = leilaoDao.total();
		assertEquals(0l,  total);
		
	}
	
	@Test
	public void deveTrazerSomenteLeiloesAntigosHaMaisDe7Dias(){
		Leilao noLimite =  leilaoXbox;
		
		Calendar dataAntiga = Calendar.getInstance();
		dataAntiga.add(Calendar.DAY_OF_MONTH, -7);
		noLimite.setDataAbertura(dataAntiga);
		usuarioDao.salvar(teste);
		leilaoDao.salvar(noLimite);
		
		
		List<Leilao> antigos = leilaoDao.antigos();
		assertEquals(1, antigos.size());
		
	}
	
	@Test
	public void deveTrazerLeiloesNaoEncerradosNoPeriodo(){
		Calendar comecoDoIntervalo = Calendar.getInstance();
		comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -2);
		Calendar fimDoIntervalo = Calendar.getInstance();
		fimDoIntervalo.add(Calendar.DAY_OF_MONTH, +2);
		usuarioDao.salvar(teste);
		Leilao leilao1 = leilaoGeladeira;
		Calendar dataLeilao1 = Calendar.getInstance();
		dataLeilao1.add(Calendar.DAY_OF_MONTH, -3);
		leilao1.setDataAbertura(dataLeilao1);
		Leilao leilao2 = leilaoXbox;
		leilao2.setDataAbertura(Calendar.getInstance());
			
		leilaoDao.salvar(leilao1);
		leilaoDao.salvar(leilao2);
		List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);
		assertEquals(1, leiloes.size());
		assertEquals("XBox",leiloes.get(0).getNome());
	}
}
