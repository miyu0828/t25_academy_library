package jp.co.metateam.library.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.AccountDto;
import jp.co.metateam.library.model.BookMst;
import jp.co.metateam.library.model.BookMstDto;
import jp.co.metateam.library.repository.BookMstRepository;

@Service
public class BookMstService {

    private final BookMstRepository bookMstRepository;
    
    @Autowired
    public BookMstService(BookMstRepository bookMstRepository){
        this.bookMstRepository = bookMstRepository;
    }
    
    public List<BookMstDto> findAvailableWithStockCount() {
        List<BookMst> books = this.bookMstRepository.findLimitedBook();
        List<BookMstDto> bookMstDtoList = new ArrayList<BookMstDto>();

        // 書籍の在庫数を取得
        // FIXME: 現状は書籍ID毎にDBに問い合わせている。一度のSQLで完了させたい。
        for (int i = 0; i < books.size(); i++) {
            BookMst book = books.get(i);
            BookMstDto bookMstDto = new BookMstDto();
            bookMstDto.setId(book.getId());
            bookMstDto.setIsbn(book.getIsbn());
            bookMstDto.setTitle(book.getTitle());
            bookMstDtoList.add(bookMstDto);
        }

        return bookMstDtoList;
    }
    

public boolean isValid( BookMstDto bookMstDto,Model model) {
      String booktitle = bookMstDto.getTitle();
      String bookisbn = bookMstDto.getIsbn();

      List<String> errtitleList = new ArrayList<>();
      List<String> errisbnList = new ArrayList<>();

      if(StringUtils.isBlank(booktitle)){
        errtitleList.add("書籍名は必須です");
    } else if (booktitle.length() > 255) {
        errtitleList.add("書籍名は255文字以内で入力してください");
        model.addAttribute("errTitle",errtitleList);
      }

      if (StringUtils.isBlank(bookisbn)) {
        errisbnList.add("ISBNは必須です");
    } else if (bookisbn.length() != 13) {
        errisbnList.add("ISBNは13文字で入力してください");
    } else if (!bookisbn.matches("^[0-9]+$")) {  // ISBNが半角数字であることを確認
        errisbnList.add("ISBNは半角数字のみで入力してください");
    }

     // バリデーションエラーがあれば、modelにエラーメッセージを設定
     if (!errtitleList.isEmpty()) {
        model.addAttribute("errtitleList", errtitleList);
    }

    if (!errisbnList.isEmpty()) {
        model.addAttribute("errisbnList", errisbnList);
    }

    // エラーがあれば、false を返して保存処理を中断
    if (!errtitleList.isEmpty() || !errisbnList.isEmpty()) {
        return true;
    }

    return false;
}

    public boolean checkIsbnEntry(String isbn, Model model) {
        // ISBNを取得
        // String isbn = bookMstDto.getIsbn();
    
        // ISBNがnullまたは空文字の場合、エラーメッセージを設定してfalseを返す
        if (StringUtils.isBlank(isbn)) {
            return true; // ISBNが空なら何もしない
        }
    
        int bookMst = this.bookMstRepository.getIsbn(isbn);

    
        if (bookMst >= 1) {
            List<String> errisbnList = new ArrayList<>();
            errisbnList.add("登録されているISBNです");
            model.addAttribute("errIsbn", errisbnList); // エラーメッセージをmodelにセット
            return true; // ISBNが重複している場合、trueを返す
        }
    
        // 重複がなければfalseを返す
        return false;
    
    }
    

    @Transactional
    public void save(BookMstDto bookMstDto ) {
        try {
            
            // AccountDtoからAccountへの変換
            BookMst book = new BookMst();

            book.setTitle(bookMstDto.getTitle());
            book.setIsbn(bookMstDto.getIsbn());

            // データベースへの保存
            this.bookMstRepository.save(book);
        } catch (Exception e) {
            throw new RuntimeException("保存処理に失敗しました", e);
        }
    }
    
}



