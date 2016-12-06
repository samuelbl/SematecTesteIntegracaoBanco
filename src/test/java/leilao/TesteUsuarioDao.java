package leilao;

import static org.junit.Assert.*;

import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import br.com.sematec.leilao.dao.CriadorDeSessao;
import br.com.sematec.leilao.dao.UsuarioDao;
import br.com.sematec.leilao.dominio.Usuario;

public class TesteUsuarioDao {
	// deveEncontrarPeloNomeEEmailMockado()
	private Session session;
	private UsuarioDao usuarioDao;

	@Before
	public void antes(){
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
	}
	
	@After
	public void depois(){
		session.close();
	}
	
	@Test
	public void deveEncontrarPeloNomeEEmailMockado() {
		Session session = Mockito.mock(Session.class);
		Query query = Mockito.mock(Query.class);
		UsuarioDao usuarioDao = new UsuarioDao(session);

		Usuario usuario = new Usuario("usuarioTeste", "usuario@teste.com");
		String sql = "from Usuario u where u.nome = :nome and u.email = :email";
		Mockito.when(session.createQuery(sql)).thenReturn(query);
		Mockito.when(query.uniqueResult()).thenReturn(usuario);
		Mockito.when(query.setParameter("nome", "usuarioTeste")).thenReturn(query);
		Mockito.when(query.setParameter("email", "usuario@teste.com.br")).thenReturn(query);
		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("usuarioTeste", "usuario@teste.com.br");
		assertEquals(usuario.getNome(), usuarioDoBanco.getNome());
		assertEquals(usuario.getEmail(), usuarioDoBanco.getEmail());
	}
	
	
	
	//Testes com banco
	
	@Test
	public void deveRetornarNuloSeNaoEncontarUsuario(){
		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("usuarioTeste", "usuario@teste.com.br");
		assertNull(usuarioDoBanco);
	}
	
	
	
	@Test
	public void deveEncontrarPeloNomeEEmail() {
		
		
		Usuario usuario = new Usuario("usuarioTeste", "usuario@teste.com");
		usuarioDao.salvar(usuario);
		Usuario usuarioDoBanco = usuarioDao.porNomeEEmail("usuarioTeste", "usuario@teste.com");
		
		assertEquals(usuario.getNome(), usuarioDoBanco.getNome());
		assertEquals(usuario.getEmail(), usuarioDoBanco.getEmail());
		usuarioDao.deletar(usuarioDoBanco);
	}
}
