
# ========================================================================================
#                                                                                 Overview
#                                                                                 ========
SAFluteは、SAStrutsをタイプセーフ拡張したフレームワークです。

SAFluteによるメリットは例えば以下のような感じ：

o フォワード先のJSPの指定をタイプセーフに (Actionのreturn指定にて)
o リダイレクト先の指定をタイプセーフに (Actionのreturn指定にて)
o 綺麗なURLのままActionクラス名はわかりやすい名前に (IndexAction地獄を回避)
o メッセージリソースを継承可能に (ドメイン間の共通化が可能に)
o properties形式のコンフィグを継承可能に (アプリ間の共通化が可能に)
o などなど

詳しくは、INTRO_ja.txt をご覧下さい。


# ========================================================================================
#                                                                              Environment
#                                                                              ===========
# ----------------------------------------------------------
#                                             Source Compile
#                                             --------------
Maven2管理されていますので、動作させるためにはM2Eなどを用意してください。

# ----------------------------------------------------------
#                                            Application Use
#                                            ---------------
jarファイルの提供はしていません。
アプリへの適用方法は、以下のいずれかとなります。

A. Eclipseのプロジェクト参照でソースコードをそのまま参照する
B. 社内フレームワークとして取り込んで自由に修正・拡張していく

フレームワーク側の修正を追従したい場合は、A が良いでしょう。
そのまま自分たちの現場にフィットした形に改造していくのも良いでしょう。
